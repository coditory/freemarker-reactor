package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
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

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

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
    private final AtomicReference<TemplateKey> parentTemplate = new AtomicReference<>();
    private final Map<TemplateKey, Set<TemplateKey>> dependencies = new ConcurrentHashMap<>();
    private final Map<TemplateKey, ResolvedTemplate> resolved = new ConcurrentHashMap<>();
    private final Set<TemplateKey> unresolved = ConcurrentHashMap.newKeySet();
    private final Set<TemplateKey> missing = ConcurrentHashMap.newKeySet();

    TemplateResolutionContext(TemplateKey mainTemplateKey, ResolvedTemplate resolvedTemplate) {
        TemplateKey mainTemplateKeyWithOutLocale = mainTemplateKey.withNoLocale();
        this.parentTemplate.set(mainTemplateKeyWithOutLocale);
        addResolvedDependency(mainTemplateKeyWithOutLocale, resolvedTemplate);
    }

    TemplateKey getCurrentTemplate(Environment env) {
        requireNonNull(env);
        String templateName = env.getCurrentTemplate().getName();
        TemplateKey templateKey = toMinimalTemplateKey(templateName);
        if (!isResolved(templateKey)) {
            throw new IllegalStateException("Expected " + templateKey + " to be resolved");
        }
        return resolved.get(templateKey).getKey();
    }

    TemplateKey getParentTemplate() {
        return parentTemplate.get();
    }

    void setParentTemplate(TemplateKey templateKey) {
        requireNonNull(templateKey);
        parentTemplate.set(templateKey.withNoLocale());
    }

    private TemplateKey toMinimalTemplateKey(String templateName) {
        TemplateKey key = getParentTemplate().withName(templateName);
        return toMinimalTemplateKey(key);
    }

    private TemplateKey toMinimalTemplateKey(TemplateKey key) {
        return key.isScoped()
                ? key.withNoLocale()
                : key.withNoLocale().withNoModule();
    }

    boolean isRegistered(TemplateKey templateKey) {
        requireNonNull(templateKey);
        TemplateKey key = toMinimalTemplateKey(templateKey);
        return unresolved.contains(key)
                || resolved.containsKey(key)
                || missing.contains(key);
    }

    void addResolvedDependency(TemplateKey templateKey, ResolvedTemplate resolvedTemplate) {
        requireNonNull(templateKey);
        requireNonNull(resolvedTemplate);
        TemplateKey minTemplateKey = toMinimalTemplateKey(templateKey);
        unresolved.remove(minTemplateKey);
        resolved.put(minTemplateKey, resolvedTemplate);
    }

    public void addMissingDependency(TemplateKey templateKey) {
        requireNonNull(templateKey);
        TemplateKey minTemplateKey = toMinimalTemplateKey(templateKey);
        unresolved.remove(minTemplateKey);
        missing.add(minTemplateKey);
    }

    boolean isResolved(TemplateKey templateKey) {
        requireNonNull(templateKey);
        TemplateKey minTemplateKey = toMinimalTemplateKey(templateKey);
        return resolved.containsKey(minTemplateKey);
    }

    boolean isMissing(TemplateKey templateKey) {
        requireNonNull(templateKey);
        TemplateKey minTemplateKey = toMinimalTemplateKey(templateKey);
        return missing.contains(minTemplateKey);
    }

    ResolvedTemplate getResolved(TemplateKey templateKey) {
        requireNonNull(templateKey);
        TemplateKey minTemplateKey = toMinimalTemplateKey(templateKey);
        return resolved.get(minTemplateKey);
    }

    Set<TemplateKey> getUnresolvedDependencies() {
        return unmodifiableSet(unresolved);
    }

    boolean hasUnresolvedDependencies() {
        return !unresolved.isEmpty();
    }

    void addDependency(TemplateKey templateKey, TemplateKey dependencyKey) {
        requireNonNull(templateKey);
        requireNonNull(dependencyKey);
        TemplateKey minTemplateKey = toMinimalTemplateKey(templateKey);
        TemplateKey minDependencyKey = toMinimalTemplateKey(dependencyKey);
        validateDependency(minTemplateKey, minDependencyKey);
        if (resolved.containsKey(minDependencyKey) || missing.contains(minDependencyKey)) {
            return;
        }
        unresolved.add(minDependencyKey);
        logger.trace("Added dependency: " + minTemplateKey + " -> " + minDependencyKey);
        dependencies.compute(minTemplateKey, (key, value) -> {
            Set<TemplateKey> values = value == null ? new HashSet<>() : value;
            values.add(minDependencyKey);
            return values;
        });
    }

    private void validateDependency(TemplateKey templateKey, TemplateKey dependency) {
        if (!dependency.isAccessibleFrom(templateKey)) {
            throw new TemplateResolutionException("Detected dependency to package scope template: " +
                    templateKey + " -> " + dependency);
        }
        if (templateKey.equals(dependency) || isCycle(templateKey, dependency)) {
            throw new TemplateResolutionException("Detected circular template dependency: " +
                    templateKey + " <-> " + dependency);
        }
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
