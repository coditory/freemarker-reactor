package com.coditory.freemarker.reactor;

import freemarker.cache.TemplateLoader;

import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ReactiveFreeMarkerLoaderAdapter implements TemplateLoader {
    private static final Pattern localePattern = Pattern.compile("(.+)(_[a-z][a-z](_[A-Z][A-Z])?)?");

    @Override
    public Object findTemplateSource(String name) {
        Matcher matcher = localePattern.matcher(name);
        if (!matcher.matches()) {
            return load(name);
        }
        return load(matcher.group(1));
    }

    private String load(String name) {
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        return context.getLoadedDependency(name);
    }

    @Override
    public long getLastModified(Object templateSource) {
        return 0;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) {
        String content = templateSource.toString();
        return new StringReader(content);
    }

    @Override
    public void closeTemplateSource(Object templateSource) {
        // deliberately empty
    }
}
