package com.coditory.freemarker.reactor;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.coditory.freemarker.reactor.TemplateConstants.INDEX_FILE;
import static com.coditory.freemarker.reactor.TemplateConstants.PROTECTED_TEMPLATE_PREFIX;
import static com.coditory.freemarker.reactor.TemplateConstants.SEPARATOR;

final class TemplateNames {
    static final Pattern MODULE_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9-_]+");
    static final Pattern TEMPLATE_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9-_/.]+");

    static String resolveTemplateDependencyName(String templateName, String dependencyName) {
        if (!dependencyName.startsWith("./") && !dependencyName.startsWith("../")) {
            return resolveTemplateName(dependencyName);
        }
        validateTemplateName(dependencyName);
        Path parent = resolveTemplatePath(templateName);
        Path resolved = parent.getNameCount() == 1
                ? Path.of(dependencyName.replace(SEPARATOR, File.separator))
                : parent.getParent().resolve(dependencyName.replace(SEPARATOR, File.separator));
        return normalize(resolved, dependencyName);
    }

    static String resolveTemplateName(String templateName) {
        Path path = resolveTemplatePath(templateName);
        return normalize(path, templateName);
    }

    static String resolveTemplateBaseName(String templateName) {
        Path path = resolveTemplatePath(templateName);
        String result = normalize(path, templateName);
        if (result.endsWith(SEPARATOR + INDEX_FILE)) {
            result = result.substring(0, result.length() - (SEPARATOR + INDEX_FILE).length());
        } else if (result.equals(INDEX_FILE)) {
            throw new IllegalArgumentException("Template name '" + templateName + "' points to base path");
        }
        return result;
    }

    private static String normalize(Path path, String templateName) {
        Path normalized = path.normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..")) {
            throw new IllegalArgumentException("Template name '" + templateName + "' points outside of the base path");
        }
        return normalized.toString()
                .replace(File.separator, SEPARATOR);
    }

    private static Path resolveTemplatePath(String templateName) {
        validateTemplateName(templateName);
        Path path = Path.of(templateName.replace(SEPARATOR, File.separator))
                .normalize();
        if (path.isAbsolute() || path.startsWith("..")) {
            throw new IllegalArgumentException("Template name '" + templateName + "' points outside of the base path");
        }
        return path;
    }

    private static void validateTemplateName(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("Expected non-empty template name. Got: '" + templateName + "'");
        }
        if (!TEMPLATE_NAME_PATTERN.matcher(templateName).matches()) {
            throw new IllegalArgumentException("Invalid character in template name: '" + templateName + "'"
                    + ". Template name must comply: " + TEMPLATE_NAME_PATTERN.pattern());
        }
        if (templateName.contains("...")) {
            throw new IllegalArgumentException("Invalid character sequence '...' in template: '" + templateName + "'");
        }
        if (templateName.contains(SEPARATOR + SEPARATOR)) {
            throw new IllegalArgumentException("Invalid character sequence '" + SEPARATOR + SEPARATOR + "' in template: '" + templateName + "'");
        }
        String[] parts = templateName.split(SEPARATOR);
        for (int i = 0; i < parts.length; ++i) {
            String part = parts[i];
            if (i == parts.length - 1) {
                if (part.length() > 1 && part.indexOf(PROTECTED_TEMPLATE_PREFIX, 1) >= 0) {
                    throw new IllegalArgumentException("Invalid character '" + PROTECTED_TEMPLATE_PREFIX + "' in template name: '" + templateName + "'"
                            + ". Use '-' instead.");
                }
            }
        }
    }

    static void validateModules(Collection<String> modules) {
        modules.forEach(TemplateNames::validateModule);
    }

    static void validateModule(String module) {
        if (module == null || module.isBlank()) {
            throw new IllegalArgumentException("Expected non-empty module name. Got: '" + module + "'");
        }
        if (!MODULE_NAME_PATTERN.matcher(module).matches()) {
            throw new IllegalArgumentException("Invalid character in module name: '" + module + "'"
                    + ". Template name must comply: " + MODULE_NAME_PATTERN.pattern());
        }
    }
}
