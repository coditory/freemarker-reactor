package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerTemplateLoader;
import com.coditory.freemarker.reactor.loader.TemplateLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class TemplateLoader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ReactiveFreeMarkerTemplateLoader loader;
    private final List<String> globalNamespaces;

    TemplateLoader(ReactiveFreeMarkerTemplateLoader loader, List<String> globalNamespaces) {
        this.loader = loader;
        this.globalNamespaces = globalNamespaces;
    }

    Mono<ResolvedTemplate> loadTemplate(TemplateKey key) {
        List<TemplateKey> keys = generateTemplateKeys(key);
        logger.debug("Looking for template {} with alternative keys {}", key, keys);
        return Flux.fromIterable(keys)
                .flatMapSequential(this::loadTemplateAndWrap)
                .next()
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new TemplateLoadingException("Missing template: '" + key + "'")))
                );
    }

    private Mono<ResolvedTemplate> loadTemplateAndWrap(TemplateKey key) {
        return loader.loadTemplate(key)
                .map(content -> new ResolvedTemplate(key, content));
    }

    private List<TemplateKey> generateTemplateKeys(TemplateKey key) {
        return generateKeysFromGlobalNamespaces(key)
                .flatMap(this::generateKeysFromLocale)
                .flatMap(this::generateKeysFromIndexes)
                .collect(toList());
    }

    private Stream<TemplateKey> generateKeysFromGlobalNamespaces(TemplateKey key) {
        if (!key.hasNamespace() || globalNamespaces.isEmpty()) {
            return Stream.of(key);
        }
        Stream<TemplateKey> nsKeys = globalNamespaces.stream()
                .map(key::withNamespace);
        return Stream.concat(Stream.of(key), nsKeys);
    }

    private Stream<TemplateKey> generateKeysFromIndexes(TemplateKey key) {
        String nameWithIndex = key.getName() + "/_index";
        return Stream.of(key, key.withName(nameWithIndex));
    }

    private Stream<TemplateKey> generateKeysFromLocale(TemplateKey key) {
        if (!key.hasLocale()) {
            return Stream.of(key);
        }
        List<TemplateKey> keys = new ArrayList<>(5);
        keys.add(key);
        Locale locale = key.getLocale();
        if (locale.getLanguage() != null) {
            keys.add(key.withLocale(Locale.forLanguageTag(locale.getLanguage())));
        }
        keys.add(key.withLocale(null));
        return keys.stream();
    }
}
