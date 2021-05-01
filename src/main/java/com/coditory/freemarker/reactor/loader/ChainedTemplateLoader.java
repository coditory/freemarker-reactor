package com.coditory.freemarker.reactor.loader;

import com.coditory.freemarker.reactor.TemplateKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ChainedTemplateLoader implements TemplateLoader {
    private final List<TemplateLoader> loaders;

    public ChainedTemplateLoader(List<TemplateLoader> loaders) {
        if (loaders == null || loaders.isEmpty()) {
            throw new IllegalArgumentException("Expected non empty list of template loaders");
        }
        this.loaders = List.copyOf(loaders);
    }

    @Override
    public Mono<String> loadTemplate(TemplateKey key) {
        return Flux.fromIterable(loaders)
                .flatMapSequential(loader -> loader.loadTemplate(key))
                .next();
    }
}
