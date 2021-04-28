package com.coditory.freemarker.reactor;

import java.util.Locale;
import java.util.Objects;

import static com.coditory.freemarker.reactor.TemplateNameResolver.resolveTemplateBaseName;
import static com.coditory.freemarker.reactor.TemplateNameResolver.resolveTemplateDependencyName;
import static com.coditory.freemarker.reactor.TemplateNameResolver.resolveTemplateName;

public final class TemplateKey {
    private final String namespace;
    private final String name;
    private final Locale locale;

    public TemplateKey(String namespace, String name, Locale locale) {
        this.namespace = namespace;
        this.name = resolveTemplateName(name);
        this.locale = locale;
    }

    public boolean isAccessibleFrom(TemplateKey other) {
        if (!name.contains("_")) {
            return true;
        }
        String[] otherParts = other.name.split("/");
        String[] parts = name.split("/");
        if (parts.length != otherParts.length) {
            return false;
        }
        int i = 0;
        while(i < parts.length - 1 && parts[i].equals(otherParts[i])) {
            ++i;
        }
        return i == parts.length - 1;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean hasNamespace() {
        return namespace != null;
    }

    public TemplateKey dependencyKey(String name) {
        String resolved = resolveTemplateDependencyName(this.name, name);
        return withName(resolved);
    }

    public TemplateKey withNamespace(String namespace) {
        return new TemplateKey(namespace, name, locale);
    }

    public TemplateKey withName(String name) {
        return new TemplateKey(namespace, name, locale);
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

    public TemplateKey withLocale(Locale locale) {
        return new TemplateKey(namespace, name, locale);
    }

    public boolean hasLocale() {
        return locale != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateKey that = (TemplateKey) o;
        return Objects.equals(namespace, that.namespace)
                && Objects.equals(name, that.name)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name, locale);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        if (namespace != null) {
            builder
                    .append(":")
                    .append(namespace)
                    .reverse();
        }
        if (locale != null) {
            builder
                    .append("(")
                    .append(locale)
                    .append(")");
        }
        return builder.toString();
    }
}
