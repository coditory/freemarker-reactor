package com.coditory.freemarker.reactor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

class TemplateNameResolver {
    static final Pattern TEMPLATE_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9-_/.]+");

    static String resolveTemplateDependencyName(String templateName, String dependencyName) {
        if (!dependencyName.startsWith("./") && !dependencyName.startsWith("../")) {
            return resolveTemplateName(dependencyName);
        }
        validateTemplateName(dependencyName);
        Path parent = resolveTemplatePath(templateName);
        Path resolved = parent.getNameCount() == 1
                ? Path.of(dependencyName)
                : parent.getParent().resolve(dependencyName);
        return normalize(resolved, dependencyName);
    }

    static String resolveTemplateName(String templateName) {
        Path path = resolveTemplatePath(templateName);
        return normalize(path, templateName);
    }

    private static String normalize(Path path, String templateName) {
        Path normalized = path.normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..")) {
            throw new IllegalArgumentException("Template name points outside base path: '" + templateName + "'");
        }
        return normalized.toString();
    }

    private static Path resolveTemplatePath(String templateName) {
        validateTemplateName(templateName);
        Path path = Path.of(templateName).normalize();
        if (path.isAbsolute() || path.startsWith("..")) {
            throw new IllegalArgumentException("Template name points outside base path: '" + templateName + "'");
        }
        return path;
    }

    private static void validateTemplateName(String templateName) {
        if (!TEMPLATE_NAME_PATTERN.matcher(templateName).matches()) {
            throw new IllegalArgumentException("Invalid character in template name: '" + templateName + "'"
                    + ". Template name must comply: " + TEMPLATE_NAME_PATTERN.pattern());
        }
        Arrays.stream(templateName.split("/")).forEach(part -> {
            if (part.length() > 1 && part.indexOf('_', 1) >= 0) {
                throw new IllegalArgumentException("Invalid character '_' in template name: '" + templateName + "'"
                        + ". Use '-' instead.");
            }
        });
    }
}
