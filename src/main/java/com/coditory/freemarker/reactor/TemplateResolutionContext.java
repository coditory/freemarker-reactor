package com.coditory.freemarker.reactor;

import freemarker.template.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toSet;

final class TemplateResolutionContext implements TemplateModel {
    private static final ThreadLocal<TemplateResolutionContext> THREAD_CONTEXT = new ThreadLocal<>();

    static void setupInThreadLocal(TemplateResolutionContext context) {
        THREAD_CONTEXT.set(context);
    }

    static void removeFromThreadLocal() {
        THREAD_CONTEXT.remove();
    }

    static TemplateResolutionContext getFromThreadLocal() {
        TemplateResolutionContext context = THREAD_CONTEXT.get();
        if (context == null) {
            throw new TemplateResolutionException("TemplateResolutionContext not found in thread local");
        }
        return context;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemplateKey templateKey;
    private final Map<TemplateKey, Set<TemplateKey>> dependencies = new ConcurrentHashMap<>();
    private final Map<TemplateKey, ResolvedTemplate> resolved = new ConcurrentHashMap<>();
    private final AtomicReference<TemplateKey> resolvedTemplate = new AtomicReference<>();
    private final Set<TemplateKey> unresolved = ConcurrentHashMap.newKeySet();

    TemplateResolutionContext(TemplateKey templateKey) {
        this.templateKey = templateKey;
        this.resolvedTemplate.set(templateKey);
    }

    public TemplateKey getTemplateKey() {
        return templateKey;
    }

    public TemplateKey getResolvedTemplate() {
        TemplateKey key = resolvedTemplate.get();
        ResolvedTemplate resolvedTemplate = resolved.get(key);
        return resolvedTemplate != null
                ? resolvedTemplate.getKey()
                : key;
    }

    public TemplateKey getResolvedTemplate(TemplateKey key) {
        ResolvedTemplate resolvedTemplate = resolved.get(key);
        return resolvedTemplate != null
                ? resolvedTemplate.getKey()
                : null;
    }

    public void setResolvedTemplate(TemplateKey templateKey) {
        logger.info("Setting resolved template: " + templateKey + ". Previous: " + resolvedTemplate.get());
        resolvedTemplate.set(templateKey);
    }

    void addResolvedDependency(TemplateKey templateKey, ResolvedTemplate resolvedTemplate) {
        unresolved.remove(templateKey);
        resolved.put(templateKey, resolvedTemplate);
    }

    boolean isLoaded(TemplateKey templateKey) {
        return resolved.containsKey(templateKey);
    }

    ResolvedTemplate getLoaded(TemplateKey templateKey) {
        return resolved.get(templateKey);
    }

    Set<TemplateKey> getUnresolvedDependencies() {
        return unresolved.stream()
                .filter(it -> !resolved.containsKey(it))
                .collect(toSet());
    }

    boolean allDependenciesLoaded() {
        return unresolved.isEmpty();
    }

    void validateDependency(TemplateKey templateKey, TemplateKey dependency) {
        if (!dependency.isAccessibleFrom(templateKey)) {
            throw new TemplateResolutionException("Detected dependency to package scope template: " +
                    templateKey + " -> " + dependency);
        }
        if (templateKey.equals(dependency) || isCycle(templateKey, dependency)) {
            throw new TemplateResolutionException("Detected circular template dependency: " +
                    templateKey + " <-> " + dependency);
        }
    }

    void addDependency(TemplateKey templateKey, TemplateKey dependencyKey) {
        if (resolved.containsKey(dependencyKey)) {
            return;
        }
        unresolved.add(dependencyKey);
        logger.info("Added dependency: " + templateKey + " -> " + dependencyKey);
        dependencies.compute(templateKey, (key, value) -> {
            Set<TemplateKey> values = value == null ? new HashSet<>() : value;
            values.add(dependencyKey);
            return values;
        });
    }

    private boolean isCycle(TemplateKey templateKey, TemplateKey templateDependency) {
        Set<TemplateKey> dependencies = getAllDependencies(templateDependency);
        return dependencies.contains(templateKey);
    }

    private Set<TemplateKey> getAllDependencies(TemplateKey templateKey) {
        return dfs(templateKey, dependencies);
    }

    private Set<TemplateKey> dfs(TemplateKey templateKey, Map<TemplateKey, Set<TemplateKey>> source) {
        Set<TemplateKey> children = source.getOrDefault(templateKey, Set.of());
        if (children.isEmpty()) {
            return children;
        }
        Set<TemplateKey> result = new HashSet<>();
        List<TemplateKey> stack = new ArrayList<>(children);
        while (!stack.isEmpty()) {
            TemplateKey dependent = stack.remove(stack.size() - 1);
            if (!result.contains(dependent)) {
                result.add(dependent);
                stack.addAll(source.getOrDefault(dependent, Set.of()));
            }
        }
        return result;
    }
}
