package com.coditory.freemarker.reactor;

import freemarker.core.InvalidReferenceException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.util.Map;

import static com.coditory.freemarker.reactor.ParametersResolver.resolveParams;
import static java.util.Objects.requireNonNull;

public final class Template {
    private final TemplateKey key;
    private final freemarker.template.Template template;
    private final TemplateDependencyResolver loader;
    private final TemplateResolutionContext context;

    Template(
            TemplateKey key,
            freemarker.template.Template template,
            TemplateDependencyResolver loader,
            TemplateResolutionContext context
    ) {
        this.key = requireNonNull(key);
        this.template = requireNonNull(template);
        this.loader = requireNonNull(loader);
        this.context = requireNonNull(context);
    }

    public Mono<String> process() {
        return process(Map.of());
    }

    public Mono<String> process(Map<String, Object> params) {
        requireNonNull(params);
        return resolveParams(params)
                .flatMap(resolvedParams -> resolveDependenciesAndProcess(context, resolvedParams));
    }

    private Mono<String> process(TemplateResolutionContext context, Map<String, Object> params) {
        return Mono.fromCallable(() -> processSync(context, params))
                .filter(it -> !context.hasUnresolvedDependencies())
                .switchIfEmpty(resolveDependenciesAndProcess(context, params));
    }

    private String processSync(TemplateResolutionContext context, Map<String, Object> params) {
        StringWriter writer = new StringWriter();
        try {
            TemplateResolutionContext.setupInThreadLocal(context);
            template.process(params, writer);
            return writer.toString();
        } catch (InvalidReferenceException e) {
            if (!context.hasUnresolvedDependencies()) {
                throw new TemplateResolutionException("Could not resolve template " + key, e);
            }
            return "";
        } catch (Exception e) {
            throw new TemplateResolutionException("Could not resolve template " + key, e);
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
        return loader.resolveDependency(templateKey)
                .doOnNext(resolved -> context.addResolvedDependency(templateKey, resolved))
                .switchIfEmpty(Mono.defer(() -> {
                    context.addMissingDependency(templateKey);
                    return Mono.empty();
                }))
                .thenReturn(templateKey)
                .onErrorMap(it -> new TemplateResolutionException(
                        "Could not resolve template " + key + ". Could not resolve template dependency " + templateKey, it));
    }
}
