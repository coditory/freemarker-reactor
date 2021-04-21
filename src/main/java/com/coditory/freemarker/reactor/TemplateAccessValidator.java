package com.coditory.freemarker.reactor;

import java.nio.file.Path;
import java.util.Set;

@FunctionalInterface
interface TemplateAccessValidator {
    boolean hasAccess(String template, String dependency);
}

class PartitioningTemplateAccessValidator implements TemplateAccessValidator {
    private final Path partitioningBasePath;
    private final Set<String> partitioningWhitelist;

    public PartitioningTemplateAccessValidator(Path partitioningBasePath, Set<String> partitioningWhitelist) {
        this.partitioningBasePath = partitioningBasePath;
        this.partitioningWhitelist = partitioningWhitelist;
    }

    @Override
    public boolean hasAccess(String template, String dependency) {
        return true;
    }
}