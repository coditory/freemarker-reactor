package com.coditory.freemarker.reactor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

final class TemplateNameResolver {
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

    static String resolveTemplateBaseName(String templateName) {
        Path path = resolveTemplatePath(templateName);
        String result = normalize(path, templateName);
        if (result.endsWith("/_index")) {
            result = result.substring(0, result.length() - "/_index".length());
        } else if (result.equals("_index")) {
            throw new IllegalArgumentException("Template name points to base path index: '" + templateName + "'");
        }
        return result;
    }

    private static String normalize(Path path, String templateName) {
        Path normalized = path.normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..")) {
            throw new IllegalArgumentException("Template name points outside base path: '" + templateName + "'");
        }
        return normalized.toString().replace('\\', '/');
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
        String[] parts = templateName.split("/");
        for (int i = 0; i < parts.length; ++i) {
            String part = parts[i];
            if (i == parts.length - 1) {
                if (part.length() > 1 && part.indexOf('_', 1) >= 0) {
                    throw new IllegalArgumentException("Invalid character '_' in template name: '" + templateName + "'"
                            + ". Use '-' instead.");
                }
            }
//            } else {
//                if (part.indexOf('_') >= 0) {
//                    throw new IllegalArgumentException("Invalid character '_' in template name: '" + templateName + "'"
//                            + ". Only files may be marked as protected.");
//                }
//            }
        }
    }
}
