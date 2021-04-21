package com.coditory.freemarker.reactor;

import freemarker.template.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
            throw new TemplateCreationException("TemplateResolutionContext not found in thread local");
        }
        return context;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String templateName;
    private final String templatePrefix;
    private final TemplateAccessValidator accessValidator;
    private final Map<String, Set<String>> dependencies = new ConcurrentHashMap<>();
    private final Map<String, String> resolved = new ConcurrentHashMap<>();

    TemplateResolutionContext(String templateName, String dependencyPrefix, TemplateAccessValidator accessValidator) {
        this.templateName = resolve(templateName);
        this.dependencyPrefix = dependencyPrefix;
        this.accessValidator = accessValidator;
    }

    void addResolvedDependency(String name, String content) {
        resolved.put(name, content);
    }

    boolean isDependencyLoaded(String templateName, String templateDependencyName) {
        String templateDependency = resolve(templateName, templateDependencyName);
        return resolved.containsKey(templateDependency);
    }

    String getLoadedDependency(String templateName, String templateDependencyName) {
        String template = resolve(this.templateName, templateName);
        String templateDependency = resolve(templateName, templateDependencyName);
        if (!accessValidator.hasAccess(template, templateDependency)) {
            throw new TemplateCreationException("Illegal access from: '" + template + "' to '" + templateDependency + "'");
        }
        return resolved.get(templateDependency);
    }

    Set<String> getUnresolvedDependencies() {
        return getAllDependencies(templateName).stream()
                .filter(it -> !resolved.containsKey(it))
                .collect(toSet());
    }

    boolean allDependenciesLoaded() {
        return getAllDependencies(templateName).stream()
                .allMatch(resolved::containsKey);
    }

    void addDependency(String templateName, String templateDependencyName) {
        String template = resolve(this.templateName, templateName);
        String templateDependency = resolve(template, templateDependencyName);
        if (isCycle(template, templateDependency)) {
            throw new TemplateCreationException("Detected circular template dependency: " +
                    template + " <-> " + templateDependency);
        }
        logger.info("Added dependency: " + template + " -> " + templateDependency);
        dependencies.compute(template, (key, value) -> {
            Set<String> values = value == null ? new HashSet<>() : value;
            values.add(templateDependency);
            return values;
        });
    }

    private boolean isCycle(String templateName, String templateDependency) {
        return getAllDependencies(templateDependency)
                .contains(templateName);
    }

    private Set<String> getAllDependencies(String templateName) {
        return dfs(templateName, dependencies);
    }

    private String resolve(String templateName) {
        return Path.of(templateName)
                .normalize()
                .toString();
    }

    private String resolve(String templateName, String dependency) {
        Path templatePath = Path.of(templateName);
        Path normalized = Path.of(dependency).normalize();
        Path resolved = dependency.startsWith("./") || dependency.startsWith("../")
                ? templatePath.resolve(normalized)
                : normalized;
        return resolved.toString();
    }

    private Set<String> dfs(String templateName, Map<String, Set<String>> source) {
        Set<String> children = source.getOrDefault(templateName, Set.of());
        if (children.isEmpty()) {
            return children;
        }
        Set<String> result = new HashSet<>();
        List<String> stack = new ArrayList<>(children);
        while (!stack.isEmpty()) {
            String dependent = stack.remove(stack.size() - 1);
            if (!result.contains(dependent)) {
                result.add(dependent);
                stack.addAll(source.getOrDefault(dependent, Set.of()));
            }
        }
        return result;
    }
}
