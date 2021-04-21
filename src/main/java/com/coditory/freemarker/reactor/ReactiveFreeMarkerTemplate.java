package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerTemplateLoader;
import freemarker.template.Template;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

public final class ReactiveFreeMarkerTemplate {
    private final String name;
    private final Template template;
    private final Locale locale;
    private final ReactiveFreeMarkerTemplateLoader loader;
    private final TemplateAccessValidator accessValidator;

    ReactiveFreeMarkerTemplate(
            String name,
            Locale locale,
            Template template,
            ReactiveFreeMarkerTemplateLoader loader,
            TemplateAccessValidator accessValidator
    ) {
        this.name = name;
        this.template = template;
        this.locale = locale;
        this.loader = loader;
        this.accessValidator = accessValidator;
    }

    public Mono<String> process() {
        return process(Map.of());
    }

    public Mono<String> process(Map<String, Object> params) {
        TemplateResolutionContext context = new TemplateResolutionContext(name, accessValidator);
        return resolveDependenciesAndProcess(context, params);
    }

    private Mono<String> process(TemplateResolutionContext context, Map<String, Object> params) {
        return Mono.fromCallable(() -> processSync(context, params))
                .filter(it -> context.allDependenciesLoaded())
                .switchIfEmpty(resolveDependenciesAndProcess(context, params));
    }

    private String processSync(TemplateResolutionContext context, Map<String, Object> params) {
        StringWriter writer = new StringWriter();
        try {
            TemplateResolutionContext.setupInThreadLocal(context);
            template.process(params, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new TemplateCreationException("Could not resolve template: " + name, e);
        } finally {
            TemplateResolutionContext.removeFromThreadLocal();
        }
    }

    private Mono<String> resolveDependenciesAndProcess(TemplateResolutionContext context, Map<String, Object> params) {
        return Flux.defer(() -> resolveDependencies(context))
                .collectList()
                .flatMap(it -> process(context, params));
    }

    private Flux<String> resolveDependencies(TemplateResolutionContext context) {
        return Flux.fromIterable(context.getUnresolvedDependencies())
                .flatMap(it -> resolveDependency(context, it));
    }

    private Mono<String> resolveDependency(TemplateResolutionContext context, String dependencyName) {
        return loader.loadTemplate(dependencyName, locale)
                .doOnNext(content -> context.addResolvedDependency(dependencyName, content))
                .thenReturn(dependencyName)
                .onErrorMap(it -> new TemplateCreationException(
                        "Could not resolve dependency '" + dependencyName + "' for template '" + name + "'", it));
    }
}
