package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.TemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static com.coditory.freemarker.reactor.TemplateConstants.INDEX_FILE;
import static com.coditory.freemarker.reactor.TemplateConstants.SEPARATOR;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class TemplateResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemplateLoader loader;
    private final List<String> commonModules;
    private final Cache<TemplateKey, ResolvedTemplate> cache;

    TemplateResolver(TemplateLoader loader, List<String> commonModules, Cache<TemplateKey, ResolvedTemplate> cache) {
        this.loader = requireNonNull(loader);
        requireNonNull(commonModules);
        this.commonModules = List.copyOf(commonModules);
        this.cache = requireNonNull(cache);
    }

    Mono<ResolvedTemplate> resolveTemplateWithCommonModules(TemplateRequest request) {
        TemplateRequest requestWithCommonModules = request.addModules(this.commonModules);
        return resolveTemplate(requestWithCommonModules);
    }

    Mono<ResolvedTemplate> resolveTemplate(TemplateRequest request) {
        List<TemplateKey> keys = generateTemplateKeys(request);
        return Flux.fromIterable(keys)
                .flatMapSequential(this::loadTemplateAndWrap)
                .next()
                .doOnNext(it -> logLoadedTemplate(request, it))
                .switchIfEmpty(Mono.defer(() -> {
                    logMissingTemplate(request, keys);
                    return Mono.empty();
                }));
    }

    private void logLoadedTemplate(TemplateRequest request, ResolvedTemplate resolvedTemplate) {
        if (!logger.isTraceEnabled() && !logger.isDebugEnabled()) {
            return;
        }
        String keyString = resolvedTemplate.getKey().toString();
        String requestString = request.toString();
        String message = keyString.equals(requestString)
                ? "Loaded template " + requestString
                : "Loaded template " + requestString + " from " + keyString;
        if (logger.isTraceEnabled()) {
            logger.trace(message + "\n" + resolvedTemplate.getContent());
        } else {
            logger.debug(message);
        }
    }

    private void logMissingTemplate(TemplateRequest request, List<TemplateKey> alternatives) {
        if (!logger.isTraceEnabled() && !logger.isDebugEnabled()) {
            return;
        }
        if (alternatives.size() > 1) {
            logger.debug("Missing template {}. No template matches: {}", request, alternatives);
        } else {
            logger.debug("Missing template {}", request);
        }
    }

    private Mono<ResolvedTemplate> loadTemplateAndWrap(TemplateKey key) {
        return loader.loadTemplate(key)
                .map(content -> new ResolvedTemplate(key, content));
    }

    private List<TemplateKey> generateTemplateKeys(TemplateRequest request) {
        return generateKeysFromModules(request.getModules(), request.getName(), request.getLocale())
                .flatMap(this::generateKeysFromLocale)
                .flatMap(this::generateKeysFromIndexes)
                .collect(toList());
    }

    private Stream<TemplateKey> generateKeysFromModules(List<String> modules, String name, Locale locale) {
        Set<String> uniqueModules = new LinkedHashSet<>(modules);
        uniqueModules.addAll(commonModules);
        if (uniqueModules.isEmpty()) {
            return Stream.of(new TemplateKey(null, name, locale));
        }
        return uniqueModules.stream()
                .map(module -> new TemplateKey(module, name, locale));
    }

    private Stream<TemplateKey> generateKeysFromIndexes(TemplateKey key) {
        String nameWithIndex = key.getName() + SEPARATOR + INDEX_FILE;
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
