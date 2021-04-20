package com.coditory.freemarker.reactor.loader;

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
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class ReactiveFreeMarkerClasspathLoader implements ReactiveFreeMarkerTemplateLoader {
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
    public Mono<String> loadTemplate(String name, Locale locale) {
        List<Path> paths = generateFileNames(name, locale);
        return Flux.fromIterable(paths)
                .flatMap(this::loadTemplate)
                .next()
                .onErrorMap(it ->
                        new TemplateLoadingException("Could not load template from classpath: '" + name + "' from: " + paths, it))
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new TemplateLoadingException("Missing template file for: '" + name + "'. Checked paths on classpath: " + paths)
                )));
    }

    private List<Path> generateFileNames(String name, Locale locale) {
        Path pathNoLocale = basePath.resolve(name + fileExtension);
        if (locale == null || locale.getLanguage().isEmpty()) {
            return List.of(pathNoLocale);
        }
        List<Path> result = new ArrayList<>();
        result.add(pathNoLocale);
        result.add(basePath.resolve(name + "_" + locale.getLanguage() + fileExtension));
        if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            result.add(basePath.resolve(name + "_" + locale.getLanguage() + "_" + locale.getCountry() + fileExtension));
        }
        Collections.reverse(result);
        return result;
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
        return Flux.using(
                () -> Files.lines(path),
                Flux::fromStream,
                stream -> close(stream, fs)
        ).collectList()
                .map(it -> String.join("\n", it))
                .doOnNext(it -> logger.info("Loaded template from classpath: " + relativePath))
                .onErrorResume(NoSuchFileException.class, e -> Mono.empty());
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

    private void close(Stream<String> stream, FileSystem fileSystem) {
        try {
            stream.close();
        } catch (Throwable e) {
            throw new TemplateLoadingException("Could not close stream when reading template from classpath", e);
        }
        if (fileSystem != null) {
            try {
                fileSystem.close();
            } catch (IOException e) {
                throw new TemplateLoadingException("Could not close file system when reading template from classpath", e);
            }
        }
    }
}
