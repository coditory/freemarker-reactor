package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import reactor.core.publisher.Mono;

import java.io.IOException;
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
    private final ReactiveFreeMarkerTemplateLoader loader;
    private final TemplateAccessValidator accessValidator;
    private final Locale defaultLocale;

    ReactiveFreeMarkerTemplateEngine(
            Configuration configuration,
            ReactiveFreeMarkerTemplateLoader loader,
            TemplateAccessValidator accessValidator,
            Locale defaultLocale
    ) {
        this.configuration = configuration;
        this.loader = loader;
        this.defaultLocale = defaultLocale;
        this.accessValidator = accessValidator;
    }

    public Mono<ReactiveFreeMarkerTemplate> createTemplate(String name) {
        return createTemplate(name, defaultLocale);
    }

    public Mono<ReactiveFreeMarkerTemplate> createTemplate(String name, Locale locale) {
        return loader.loadTemplate(name, locale)
                .map(content -> createTemplate(name, locale, content));
    }

    private ReactiveFreeMarkerTemplate createTemplate(String name, Locale locale, String content) {
        try {
            Template template = new Template(name, content, configuration);
            if (locale != null) {
                template.setLocale(locale);
            }
            return new ReactiveFreeMarkerTemplate(name, locale, template, loader, accessValidator);
        } catch (IOException e) {
            throw new TemplateCreationException("Could not create template: " + name);
        }
    }
}
