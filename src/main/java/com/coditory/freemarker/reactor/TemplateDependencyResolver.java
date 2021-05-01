package com.coditory.freemarker.reactor;

import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

final class TemplateDependencyResolver {
    private final TemplateRequest request;
    private final TemplateResolver resolver;

    TemplateDependencyResolver(TemplateRequest request, TemplateResolver resolver) {
        this.request = requireNonNull(request);
        this.resolver = requireNonNull(resolver);
    }

    Mono<ResolvedTemplate> resolveDependency(TemplateKey key) {
        requireNonNull(key);
        if (key.isScoped()) {
            TemplateRequest request = this.request
                    .withName(key.getName())
                    .withModule(key.getModule());
            return resolver.resolveTemplate(request);
        }
        TemplateRequest request = this.request.withName(key.getName());
        return resolver.resolveTemplateWithCommonModules(request);
    }
}
