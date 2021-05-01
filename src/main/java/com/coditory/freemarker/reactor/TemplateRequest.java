package com.coditory.freemarker.reactor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.coditory.freemarker.reactor.TemplateNames.resolveTemplateBaseName;
import static com.coditory.freemarker.reactor.TemplateNames.resolveTemplateName;
import static com.coditory.freemarker.reactor.TemplateNames.validateModules;
import static java.util.Objects.requireNonNull;

public final class TemplateRequest {
    private final List<String> modules;
    private final String name;
    private final Locale locale;

    private TemplateRequest(String name, List<String> modules, Locale locale) {
        this.name = requireNonNull(name);
        this.modules = modules;
        this.locale = locale;
    }

    public List<String> getModules() {
        return modules;
    }

    public boolean hasModules() {
        return !modules.isEmpty();
    }

    public String getName() {
        return name;
    }

    public String getTemplateBaseName() {
        return resolveTemplateBaseName(name);
    }

    public Locale getLocale() {
        return locale;
    }

    public TemplateRequest withModules(List<String> modules) {
        return Objects.equals(this.modules, modules)
                ? this
                : new TemplateRequest(name, modules, locale);
    }

    public TemplateRequest addModules(List<String> modules) {
        if (modules.isEmpty()) {
            return this;
        }
        Set<String> result = new LinkedHashSet<>(this.modules);
        result.addAll(modules);
        List<String> combinedModules = new ArrayList<>(result);
        return Objects.equals(this.modules, combinedModules)
                ? this
                : new TemplateRequest(name, combinedModules, locale);
    }

    public TemplateRequest withModule(String module) {
        List<String> modules = module == null
                ? List.of()
                : List.of(module);
        return withModules(modules);
    }

    public TemplateRequest withName(String name) {
        return Objects.equals(this.name, name)
                ? this
                : new TemplateRequest(name, modules, locale);
    }

    public TemplateRequest withLocale(Locale locale) {
        return Objects.equals(this.locale, locale)
                ? this
                : new TemplateRequest(name, modules, locale);
    }

    public boolean hasLocale() {
        return locale != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateRequest that = (TemplateRequest) o;
        return Objects.equals(modules, that.modules)
                && Objects.equals(name, that.name)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modules, name, locale);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("'");
        if (!modules.isEmpty()) {
            builder
                    .append(String.join(",", modules))
                    .append(":");
        }
        builder.append(name);
        builder.append("'");
        if (locale != null) {
            builder
                    .append("(")
                    .append(locale)
                    .append(")");
        }
        return builder.toString();
    }

    public static TemplateRequestBuilder builder(String name) {
        return new TemplateRequestBuilder(name);
    }

    public static class TemplateRequestBuilder {
        private final String name;
        private List<String> modules = List.of();
        private Locale locale;

        TemplateRequestBuilder(String name) {
            this.name = resolveTemplateName(name);
        }

        public TemplateRequestBuilder setModules(List<String> modules) {
            requireNonNull(modules);
            validateModules(modules);
            this.modules = List.copyOf(modules);
            return this;
        }

        public TemplateRequestBuilder setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public TemplateRequest build() {
            return new TemplateRequest(name, modules, locale);
        }
    }
}
