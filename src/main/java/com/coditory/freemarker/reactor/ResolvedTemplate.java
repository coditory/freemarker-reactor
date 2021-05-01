package com.coditory.freemarker.reactor;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class ResolvedTemplate {
    private final TemplateKey key;
    private final String content;

    public ResolvedTemplate(TemplateKey key, String content) {
        this.key = requireNonNull(key);
        this.content = requireNonNull(content);
    }

    public TemplateKey getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolvedTemplate that = (ResolvedTemplate) o;
        return Objects.equals(key, that.key) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, content);
    }

    @Override
    public String toString() {
        return "ResolvedTemplate{" +
                "key=" + key +
                ", content='" + content + '\'' +
                '}';
    }
}
