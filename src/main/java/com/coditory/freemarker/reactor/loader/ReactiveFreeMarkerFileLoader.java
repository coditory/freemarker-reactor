package com.coditory.freemarker.reactor.loader;

import com.coditory.freemarker.reactor.TemplateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;

public final class ReactiveFreeMarkerFileLoader implements ReactiveFreeMarkerTemplateLoader {
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
    public Mono<String> loadTemplate(TemplateKey key) {
        Path path = generateFileName(key);
        return Mono.just(path)
                .flatMap(this::loadTemplate)
                .onErrorMap(it -> new TemplateLoadingException("Could not load template " + key + " from " + path, it))
                .doOnNext(it -> logger.trace("Loaded template {} from file {}", key, path))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.trace("Could not find template {} file: {}", key, path);
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

    private Mono<String> loadTemplate(Path path) {
        return FileReader.readText(path)
                .onErrorResume(NoSuchFileException.class, e -> Mono.empty());
    }
}
