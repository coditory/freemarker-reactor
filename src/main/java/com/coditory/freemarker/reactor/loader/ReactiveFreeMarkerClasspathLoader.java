package com.coditory.freemarker.reactor.loader;

import com.coditory.freemarker.reactor.TemplateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public final class ReactiveFreeMarkerClasspathLoader implements ReactiveFreeMarkerTemplateLoader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ClassLoader classLoader;
    private final Path basePath;
    private final String fileExtension;

    public ReactiveFreeMarkerClasspathLoader() {
        this("");
    }

    public ReactiveFreeMarkerClasspathLoader(String basePath) {
        this(basePath, ".ftl", ReactiveFreeMarkerClasspathLoader.class.getClassLoader());
    }

    public ReactiveFreeMarkerClasspathLoader(String basePath, String fileExtension, ClassLoader classLoader) {
        this.basePath = Path.of(basePath);
        this.fileExtension = fileExtension;
        this.classLoader = classLoader;
    }

    @Override
    public Mono<String> loadTemplate(TemplateKey key) {
        Path path = generateFileName(key);
        return Mono.just(path)
                .flatMap(this::loadTemplate)
                .onErrorMap(it -> new TemplateLoadingException("Could not load template from classpath: '" + key + "' from: " + path, it))
                .doOnNext(it -> logger.debug("Loaded template from classpath: " + key + "', path: " + path))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.debug("Could not find template on classpath: " + key + "'. Checked path: " + path);
                    return Mono.empty();
                }));
    }

    private Path generateFileName(TemplateKey key) {
        String name = key.getName();
        Locale locale = key.getLocale();
        Path basePathWithNamespace = key.getNamespace() != null
                ? basePath.resolve(key.getNamespace())
                : basePath;
        if (locale == null || locale.getLanguage().isEmpty()) {
            return basePathWithNamespace.resolve(name + fileExtension);
        }
        if (!locale.getCountry().isEmpty()) {
            return basePathWithNamespace.resolve(name + "_" + locale.getLanguage() + "_" + locale.getCountry() + fileExtension);
        }
        return basePathWithNamespace.resolve(name + "_" + locale.getLanguage() + fileExtension);
    }

    private Mono<String> loadTemplate(Path relativePath) {
        URI uri = resolveClassPathFile(relativePath);
        if (uri == null) {
            return Mono.empty();
        }
        FileSystem fs = uri.toString().startsWith("jar:file:/")
                ? initFileSystem(uri)
                : null;
        Path path = Path.of(uri);
        return FileReader.readText(path)
                .onErrorResume(NoSuchFileException.class, e -> Mono.empty())
                .doFinally(it -> close(fs));
    }

    private URI resolveClassPathFile(Path relativePath) {
        try {
            URL url = classLoader.getResource(relativePath.toString());
            return url != null ? url.toURI() : null;
        } catch (URISyntaxException e) {
            throw new TemplateLoadingException("Could not resolve uri to classpath file with a template: " + relativePath);
        }
    }

    private FileSystem initFileSystem(URI uri) {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            try {
                return FileSystems.newFileSystem(uri, Map.of("create", "true"));
            } catch (IOException ioException) {
                throw new TemplateLoadingException("Could not create file system to read file from classpath: " + uri);
            }
        }
    }

    private void close(FileSystem fileSystem) {
        if (fileSystem != null) {
            try {
                fileSystem.close();
            } catch (IOException e) {
                throw new TemplateLoadingException("Could not close file system when reading template from classpath", e);
            }
        }
    }
}
