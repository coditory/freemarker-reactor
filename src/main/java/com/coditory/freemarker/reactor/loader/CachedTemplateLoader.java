package com.coditory.freemarker.reactor.loader;

import com.coditory.freemarker.reactor.Cache;
import com.coditory.freemarker.reactor.TemplateKey;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

public final class CachedTemplateLoader implements TemplateLoader {
    private final Cache<TemplateKey, String> cache;
    private final TemplateLoader loader;

    public CachedTemplateLoader(TemplateLoader loader) {
        this(loader, Cache.concurrentMapCache());
    }

    public CachedTemplateLoader(TemplateLoader loader, Cache<TemplateKey, String> cache) {
        if (loader instanceof CachedTemplateLoader) {
            throw new IllegalStateException("Template loader is already cached: " + loader);
        }
        this.loader = requireNonNull(loader);
        this.cache = requireNonNull(cache);
    }

    @Override
    public Mono<String> loadTemplate(TemplateKey key) {
        return cache.getOrLoad(key, loader::loadTemplate);
    }
}
