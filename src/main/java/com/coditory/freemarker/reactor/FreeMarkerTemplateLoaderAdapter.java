package com.coditory.freemarker.reactor;

import freemarker.cache.TemplateLoader;

import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

final class FreeMarkerTemplateLoaderAdapter implements TemplateLoader {
    private static final Pattern localePattern = Pattern.compile("(.+)(_[a-z][a-z](_[A-Z][A-Z])?)?");

    @Override
    public Object findTemplateSource(String name) {
        requireNonNull(name);
        Matcher matcher = localePattern.matcher(name);
        return matcher.matches()
                ? load(matcher.group(1))
                : load(name);
    }

    private String load(String name) {
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        TemplateKey key = context.getParentTemplate().withName(name);
        if (!context.isRegistered(key)) {
            throw new TemplateResolutionException(
                    "Template dependency not recognized. " +
                            "Use reactive directives <@include ...>, <@import ...> instead of " +
                            "synchronous <#include ...>, <#import ...>"
            );
        }
        ResolvedTemplate resolvedTemplate = context.getResolved(key);
        if (resolvedTemplate == null) {
            return null;
        }
        return resolvedTemplate.getContent();
    }

    @Override
    public long getLastModified(Object templateSource) {
        // deliberately empty
        return 0;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) {
        requireNonNull(templateSource);
        String content = templateSource.toString();
        return new StringReader(content);
    }

    @Override
    public void closeTemplateSource(Object templateSource) {
        // deliberately empty
    }
}
