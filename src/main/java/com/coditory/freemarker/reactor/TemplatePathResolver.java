package com.coditory.freemarker.reactor;

import java.nio.file.Path;

final class TemplatePathResolver {
    String resolve(String templatePath) {
        return Path.of(templatePath)
                .normalize()
                .toString();
    }

    String resolve(String templatePath, String dependency) {
        Path normalized = Path.of(dependency).normalize();
        Path resolved = dependency.startsWith("./") || dependency.startsWith("../")
                ? Path.of(templatePath).resolve(normalized)
                : normalized;
        return resolved.toString();
    }
}
