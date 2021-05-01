package com.coditory.freemarker.reactor;

import java.util.Locale;
import java.util.Objects;

import static com.coditory.freemarker.reactor.TemplateConstants.SEPARATOR;
import static com.coditory.freemarker.reactor.TemplateNames.resolveTemplateBaseName;
import static com.coditory.freemarker.reactor.TemplateNames.resolveTemplateDependencyName;
import static com.coditory.freemarker.reactor.TemplateNames.resolveTemplateName;

public final class TemplateKey {
    private final String module;
    private final String name;
    private final Locale locale;

    public TemplateKey(String module, String name, Locale locale) {
        this.module = module;
        this.name = resolveTemplateName(name);
        this.locale = locale;
    }

    public boolean isAccessibleFrom(TemplateKey other) {
        if (!name.contains("_")) {
            return true;
        }
        String[] otherParts = other.name.split(SEPARATOR);
        String[] parts = name.split(SEPARATOR);
        if (parts.length != otherParts.length) {
            return false;
        }
        int i = 0;
        while (i < parts.length - 1 && parts[i].equals(otherParts[i])) {
            ++i;
        }
        return i == parts.length - 1;
    }

    public boolean isScoped() {
        return name.contains("_");
    }

    public String getModule() {
        return module;
    }

    public boolean hasModule() {
        return module != null;
    }

    public TemplateKey dependencyKey(String name) {
        String resolved = resolveTemplateDependencyName(this.name, name);
        return withName(resolved);
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

    public TemplateKey withModule(String module) {
        return Objects.equals(this.module, module)
                ? this
                : new TemplateKey(module, name, locale);
    }

    public TemplateKey withName(String name) {
        return Objects.equals(this.name, name)
                ? this
                : new TemplateKey(module, name, locale);
    }

    public TemplateKey withLocale(Locale locale) {
        return Objects.equals(this.locale, locale)
                ? this
                : new TemplateKey(module, name, locale);
    }

    public TemplateKey withNoLocale() {
        return withLocale(null);
    }

    public TemplateKey withNoModule() {
        return withModule(null);
    }

    public boolean hasLocale() {
        return locale != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateKey that = (TemplateKey) o;
        return Objects.equals(module, that.module)
                && Objects.equals(name, that.name)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, name, locale);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("'");
        if (module != null) {
            builder
                    .append(module)
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
}
