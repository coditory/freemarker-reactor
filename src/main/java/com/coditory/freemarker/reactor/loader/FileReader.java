package com.coditory.freemarker.reactor.loader;

import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;

final class FileReader {
    public static final int DEFAULT_BUFFER_SIZE = 256;

    public static Mono<String> readText(Path path) {
        return readText(path, UTF_8);
    }

    public static Mono<String> readText(Path path, Charset charset) {
        return read(path)
                .reduce(DataBuffer::write)
                .map(buffer -> charset.decode(buffer.asByteBuffer()).toString());
    }

    private static Flux<DataBuffer> read(Path path) {
        return Flux.using(
                () -> AsynchronousFileChannel.open(path, StandardOpenOption.READ),
                channel -> Flux.create(sink -> {
                    ReadCompletionHandler handler =
                            new ReadCompletionHandler(channel, sink, 0, DEFAULT_BUFFER_SIZE);
                    sink.onCancel(handler::cancel);
                    sink.onRequest(handler::request);
                }),
                channel -> {
                    // Do not close channel from here, rather wait for the current read callback
                    // and then complete after releasing the DataBuffer.
                });
    }

    private static class ReadCompletionHandler implements CompletionHandler<Integer, DataBuffer> {
        private final AsynchronousFileChannel channel;
        private final FluxSink<DataBuffer> sink;
        private final int bufferSize;
        private final AtomicLong position;
        private final AtomicBoolean reading = new AtomicBoolean();
        private final AtomicBoolean disposed = new AtomicBoolean();

        public ReadCompletionHandler(
                AsynchronousFileChannel channel,
                FluxSink<DataBuffer> sink, long position, int bufferSize) {
            this.channel = channel;
            this.sink = sink;
            this.position = new AtomicLong(position);
            this.bufferSize = bufferSize;
        }

        public void read() {
            if (this.sink.requestedFromDownstream() > 0 &&
                    isNotDisposed() &&
                    this.reading.compareAndSet(false, true)) {
                DataBuffer dataBuffer = allocateBuffer(this.bufferSize);
                ByteBuffer byteBuffer = dataBuffer.asByteBuffer(0, this.bufferSize);
                this.channel.read(byteBuffer, this.position.get(), dataBuffer, this);
            }
        }

        private DataBuffer allocateBuffer(int initialCapacity) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(initialCapacity);
            return DataBuffer.fromEmptyByteBuffer(byteBuffer);
        }

        @Override
        public void completed(Integer read, DataBuffer dataBuffer) {
            if (isNotDisposed()) {
                if (read != -1) {
                    this.position.addAndGet(read);
                    dataBuffer.writePosition(read);
                    this.sink.next(dataBuffer);
                    this.reading.set(false);
                    read();
                } else {
                    closeChannel(this.channel);
                    if (this.disposed.compareAndSet(false, true)) {
                        this.sink.complete();
                    }
                    this.reading.set(false);
                }
            } else {
                closeChannel(this.channel);
                this.reading.set(false);
            }
        }

        private void closeChannel(Channel channel) {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
        }

        @Override
        public void failed(Throwable exc, DataBuffer dataBuffer) {
            closeChannel(this.channel);
            if (this.disposed.compareAndSet(false, true)) {
                this.sink.error(exc);
            }
            this.reading.set(false);
        }

        public void request(long n) {
            read();
        }

        public void cancel() {
            if (this.disposed.compareAndSet(false, true)) {
                if (!this.reading.get()) {
                    closeChannel(this.channel);
                }
            }
        }

        private boolean isNotDisposed() {
            return !this.disposed.get();
        }
    }

    private static class DataBuffer {
        private static final int MAX_CAPACITY = Integer.MAX_VALUE;
        private static final int CAPACITY_THRESHOLD = 1024 * 1024 * 4;

        private ByteBuffer byteBuffer;
        private int capacity;
        private int readPosition;
        private int writePosition;

        private DataBuffer(ByteBuffer byteBuffer) {
            if (byteBuffer == null) {
                throw new IllegalArgumentException("ByteBuffer must not be null");
            }
            ByteBuffer slice = byteBuffer.slice();
            this.byteBuffer = slice;
            this.capacity = slice.remaining();
        }

        static DataBuffer fromEmptyByteBuffer(ByteBuffer byteBuffer) {
            return new DataBuffer(byteBuffer);
        }

        private void setNativeBuffer(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
            this.capacity = byteBuffer.remaining();
        }

        public int readableByteCount() {
            return this.writePosition - this.readPosition;
        }

        public int writableByteCount() {
            return this.capacity - this.writePosition;
        }

        private int readPosition() {
            return this.readPosition;
        }

        private void readPosition(int readPosition) {
            assertIndex(readPosition >= 0, "'readPosition' %d must be >= 0", readPosition);
            assertIndex(readPosition <= this.writePosition, "'readPosition' %d must be <= %d",
                    readPosition, this.writePosition);
            this.readPosition = readPosition;
        }

        private int writePosition() {
            return this.writePosition;
        }

        public void writePosition(int writePosition) {
            assertIndex(writePosition >= this.readPosition, "'writePosition' %d must be >= %d",
                    writePosition, this.readPosition);
            assertIndex(writePosition <= this.capacity, "'writePosition' %d must be <= %d",
                    writePosition, this.capacity);
            this.writePosition = writePosition;
        }

        private int getCapacity() {
            return this.capacity;
        }

        private void changeCapacity(int newCapacity) {
            if (newCapacity <= 0) {
                throw new IllegalArgumentException(String.format("'newCapacity' %d must be higher than 0", newCapacity));
            }
            int readPosition = readPosition();
            int writePosition = writePosition();
            int oldCapacity = getCapacity();

            if (newCapacity > oldCapacity) {
                ByteBuffer oldBuffer = this.byteBuffer;
                ByteBuffer newBuffer = allocate(newCapacity);
                oldBuffer.position(0).limit(oldBuffer.capacity());
                newBuffer.position(0).limit(oldBuffer.capacity());
                newBuffer.put(oldBuffer);
                newBuffer.clear();
                setNativeBuffer(newBuffer);
            } else if (newCapacity < oldCapacity) {
                ByteBuffer oldBuffer = this.byteBuffer;
                ByteBuffer newBuffer = allocate(newCapacity);
                if (readPosition < newCapacity) {
                    if (writePosition > newCapacity) {
                        writePosition = newCapacity;
                        writePosition(writePosition);
                    }
                    oldBuffer.position(readPosition).limit(writePosition);
                    newBuffer.position(readPosition).limit(writePosition);
                    newBuffer.put(oldBuffer);
                    newBuffer.clear();
                } else {
                    readPosition(newCapacity);
                    writePosition(newCapacity);
                }
                setNativeBuffer(newBuffer);
            }
        }

        private void ensureCapacity(int length) {
            if (length > writableByteCount()) {
                int newCapacity = calculateCapacity(this.writePosition + length);
                changeCapacity(newCapacity);
            }
        }

        private static ByteBuffer allocate(int capacity) {
            return ByteBuffer.allocate(capacity);
        }

        public DataBuffer write(DataBuffer... buffers) {
            if (buffers != null && buffers.length > 0) {
                write(Arrays.stream(buffers).map(DataBuffer::asByteBuffer).toArray(ByteBuffer[]::new));
            }
            return this;
        }

        private DataBuffer write(ByteBuffer... buffers) {
            if (buffers != null && buffers.length > 0) {
                int capacity = Arrays.stream(buffers).mapToInt(ByteBuffer::remaining).sum();
                ensureCapacity(capacity);
                Arrays.stream(buffers).forEach(this::write);
            }
            return this;
        }

        private void write(ByteBuffer source) {
            int length = source.remaining();
            ByteBuffer tmp = this.byteBuffer.duplicate();
            int limit = this.writePosition + source.remaining();
            tmp.clear().position(this.writePosition).limit(limit);
            tmp.put(source);
            this.writePosition += length;
        }

        public ByteBuffer asByteBuffer() {
            return asByteBuffer(this.readPosition, readableByteCount());
        }

        public ByteBuffer asByteBuffer(int index, int length) {
            checkIndex(index, length);
            ByteBuffer duplicate = this.byteBuffer.duplicate();
            duplicate.position(index);
            duplicate.limit(index + length);
            return duplicate.slice();
        }

        private int calculateCapacity(int neededCapacity) {
            if (neededCapacity < 0) {
                throw new IllegalArgumentException("'neededCapacity' must >= 0");
            }

            if (neededCapacity == CAPACITY_THRESHOLD) {
                return CAPACITY_THRESHOLD;
            }
            if (neededCapacity > CAPACITY_THRESHOLD) {
                int newCapacity = neededCapacity / CAPACITY_THRESHOLD * CAPACITY_THRESHOLD;
                if (newCapacity > MAX_CAPACITY - CAPACITY_THRESHOLD) {
                    newCapacity = MAX_CAPACITY;
                } else {
                    newCapacity += CAPACITY_THRESHOLD;
                }
                return newCapacity;
            }
            int newCapacity = 64;
            while (newCapacity < neededCapacity) {
                newCapacity <<= 1;
            }
            return newCapacity;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof DataBuffer)) {
                return false;
            }
            DataBuffer otherBuffer = (DataBuffer) other;
            return (this.readPosition == otherBuffer.readPosition &&
                    this.writePosition == otherBuffer.writePosition &&
                    this.byteBuffer.equals(otherBuffer.byteBuffer));
        }

        @Override
        public int hashCode() {
            return this.byteBuffer.hashCode();
        }

        @Override
        public String toString() {
            return String.format("DataBuffer (r: %d, w: %d, c: %d)",
                    this.readPosition, this.writePosition, this.capacity);
        }


        private void checkIndex(int index, int length) {
            assertIndex(index >= 0, "index %d must be >= 0", index);
            assertIndex(length >= 0, "length %d must be >= 0", index);
            assertIndex(index <= this.capacity, "index %d must be <= %d", index, this.capacity);
            assertIndex(length <= this.capacity, "length %d must be <= %d", index, this.capacity);
        }

        private void assertIndex(boolean expression, String format, Object... args) {
            if (!expression) {
                String message = String.format(format, args);
                throw new IndexOutOfBoundsException(message);
            }
        }
    }
}