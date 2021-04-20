package com.coditory.freemarker.reactor.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.BaseStream;

public class ReactiveFreeMarkerFileLoader implements ReactiveFreeMarkerTemplateLoader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Path basePath;
    private final String fileExtension;

    public ReactiveFreeMarkerFileLoader(Path basePath) {
        this(basePath, ".ftl");
    }

    public ReactiveFreeMarkerFileLoader(Path basePath, String fileExtension) {
        this.basePath = basePath;
        this.fileExtension = fileExtension;
    }

    @Override
    public Mono<String> loadTemplate(String name, Locale locale) {
        List<Path> paths = generateFileNames(name, locale);
        return Flux.fromIterable(paths)
                .flatMap(this::loadTemplate)
                .next()
                .onErrorMap(it ->
                        new TemplateLoadingException("Could not load template: '" + name + "' from: " + paths, it))
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new TemplateLoadingException("Missing template file for: '" + name + "'. Checked paths: " + paths)
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

    private Mono<String> loadTemplate(Path path) {
        return Flux.using(
                () -> Files.lines(path),
                Flux::fromStream,
                BaseStream::close
        ).collectList()
                .map(it -> String.join("\n", it))
                .doOnNext(it -> logger.info("Loaded template: " + path))
                .onErrorResume(NoSuchFileException.class, e -> Mono.empty());
    }
}
