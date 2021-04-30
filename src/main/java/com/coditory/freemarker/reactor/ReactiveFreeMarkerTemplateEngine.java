package com.coditory.freemarker.reactor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import reactor.core.publisher.Mono;

import java.util.Locale;

public final class ReactiveFreeMarkerTemplateEngine {
    public static ReactiveFreeMarkerTemplateEngineBuilder builder(Version version) {
        return new ReactiveFreeMarkerTemplateEngineBuilder(version);
    }

    public static ReactiveFreeMarkerTemplateEngineBuilder builder() {
        return builder(Configuration.VERSION_2_3_31);
    }

    public static ReactiveFreeMarkerTemplateEngine createWithDefault() {
        return builder(Configuration.VERSION_2_3_31)
                .build();
    }

    private final Configuration configuration;
    private final TemplateLoader loader;
    private final Locale defaultLocale;

    ReactiveFreeMarkerTemplateEngine(
            Configuration configuration,
            TemplateLoader loader,
            Locale defaultLocale
    ) {
        this.configuration = configuration;
        this.loader = loader;
        this.defaultLocale = defaultLocale;
    }

    public Mono<ReactiveFreeMarkerTemplate> createTemplate(String name) {
        return createTemplate(name, defaultLocale);
    }

    public Mono<ReactiveFreeMarkerTemplate> createTemplate(String name, Locale locale) {
        return createTemplate(null, name, locale);
    }

    public Mono<ReactiveFreeMarkerTemplate> createTemplate(String namespace, String name) {
        return createTemplate(namespace, name, null);
    }

    public Mono<ReactiveFreeMarkerTemplate> createTemplate(String namespace, String name, Locale locale) {
        return createTemplate(new TemplateKey(namespace, name, locale));
    }

    public Mono<ReactiveFreeMarkerTemplate> createTemplate(TemplateKey key) {
        return loader.loadTemplate(key)
                .map(resolved -> createTemplate(key, resolved));
    }

    @SuppressWarnings("deprecation")
    private ReactiveFreeMarkerTemplate createTemplate(TemplateKey key, ResolvedTemplate resolved) {
        try {
            Template template = new Template(key.getName(), resolved.getContent(), configuration);
            if (key.hasLocale()) {
                template.setLocale(key.getLocale());
            }
            return new ReactiveFreeMarkerTemplate(key, template, loader);
        } catch (Exception e) {
            throw new TemplateCreationException("Could not create template " + key, e);
        }
    }
}
