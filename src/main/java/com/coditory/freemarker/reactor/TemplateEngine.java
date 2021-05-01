package com.coditory.freemarker.reactor;

import freemarker.template.Configuration;
import freemarker.template.Version;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static java.util.Objects.requireNonNull;

public final class TemplateEngine {
    public static TemplateEngineBuilder builder(Version version) {
        return new TemplateEngineBuilder(version);
    }

    public static TemplateEngineBuilder builder() {
        return builder(Configuration.VERSION_2_3_31);
    }

    public static TemplateEngine create() {
        return builder(Configuration.VERSION_2_3_31)
                .build();
    }

    private final Configuration configuration;
    private final TemplateResolver loader;
    private final Locale defaultLocale;

    TemplateEngine(
            Configuration configuration,
            TemplateResolver loader,
            Locale defaultLocale
    ) {
        this.configuration = requireNonNull(configuration);
        this.loader = requireNonNull(loader);
        this.defaultLocale = defaultLocale;
    }

    public Mono<Template> createTemplate(String name) {
        requireNonNull(name);
        return createTemplate(name, defaultLocale);
    }

    public Mono<Template> createTemplate(String name, Locale locale) {
        requireNonNull(name);
        TemplateRequest request = TemplateRequest.builder(name)
                .setLocale(locale)
                .build();
        return createTemplate(request);
    }

    public Mono<Template> createTemplate(TemplateRequest request) {
        requireNonNull(request);
        return loader.resolveTemplate(request)
                .map(resolved -> createTemplate(request, resolved))
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new TemplateCreationException("Could not resolve template: " + request))
                ));
    }

    private Template createTemplate(TemplateRequest request, ResolvedTemplate resolved) {
        TemplateKey key = resolved.getKey();
        try {
            freemarker.template.Template template = new freemarker.template.Template(key.getName(), resolved.getContent(), configuration);
            if (key.hasLocale()) {
                template.setLocale(key.getLocale());
            }
            TemplateDependencyResolver dependencyLoader = new TemplateDependencyResolver(request, loader);
            TemplateResolutionContext context = new TemplateResolutionContext(key, resolved);
            return new Template(key, template, dependencyLoader, context);
        } catch (Exception e) {
            throw new TemplateCreationException("Could not create template " + key, e);
        }
    }
}
