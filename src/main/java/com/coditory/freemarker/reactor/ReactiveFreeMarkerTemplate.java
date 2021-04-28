package com.coditory.freemarker.reactor;

import freemarker.core.InvalidReferenceException;
import freemarker.template.Template;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.util.Map;

public final class ReactiveFreeMarkerTemplate {
    private final TemplateKey key;
    private final Template template;
    private final TemplateLoader loader;

    ReactiveFreeMarkerTemplate(
            TemplateKey key,
            Template template,
            TemplateLoader loader
    ) {
        this.key = key;
        this.template = template;
        this.loader = loader;
    }

    public Mono<String> process() {
        return process(Map.of());
    }

    public Mono<String> process(Map<String, Object> params) {
        TemplateResolutionContext context = new TemplateResolutionContext(key);
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
        } catch (InvalidReferenceException e) {
            if (context.allDependenciesLoaded()) {
                throw new TemplateResolutionException("Could not resolve template: '" + key + "'", e);
            }
            return "";
        } catch (Exception e) {
            throw new TemplateResolutionException("Could not resolve template: '" + key + "'", e);
        } finally {
            TemplateResolutionContext.removeFromThreadLocal();
        }
    }

    private Mono<String> resolveDependenciesAndProcess(TemplateResolutionContext context, Map<String, Object> params) {
        return Flux.defer(() -> resolveDependencies(context))
                .collectList()
                .flatMap(it -> process(context, params));
    }

    private Flux<TemplateKey> resolveDependencies(TemplateResolutionContext context) {
        return Flux.fromIterable(context.getUnresolvedDependencies())
                .flatMap(it -> resolveDependency(context, it));
    }

    private Mono<TemplateKey> resolveDependency(TemplateResolutionContext context, TemplateKey templateKey) {
        return loader.loadTemplate(templateKey)
                .doOnNext(resolved -> context.addResolvedDependency(templateKey, resolved))
                .thenReturn(templateKey)
                .onErrorMap(it -> new TemplateResolutionException(
                        "Could not resolve template '" + key + "'. Could not resolve template dependency '" + templateKey + "'", it));
    }
}
