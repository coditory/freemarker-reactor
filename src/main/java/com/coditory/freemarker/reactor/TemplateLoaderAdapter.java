package com.coditory.freemarker.reactor;

import freemarker.cache.TemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TemplateLoaderAdapter implements TemplateLoader {
    private static final Pattern localePattern = Pattern.compile("(.+)(_[a-z][a-z](_[A-Z][A-Z])?)?");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public Object findTemplateSource(String name) {
        Matcher matcher = localePattern.matcher(name);
        return matcher.matches()
                ? load(matcher.group(1))
                : load(name);
    }

    private String load(String name) {
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        TemplateKey key = context.getDependentTemplate().withName(name);
        if (!context.isRegistered(key)) {
            throw new TemplateResolutionException(
                    "Template dependency not recognized. " +
                            "Use reactive directives <@include ...>, <@import ...> instead of " +
                            "synchronous <#include ...>, <#import ...>"
            );
        }
        ResolvedTemplate resolvedTemplate = context.getLoaded(key);
        if (resolvedTemplate == null) {
            logger.trace("Missing dependency {} for {}", key, context.getDependentTemplate());
            return null;
        }
        String result = context.getLoaded(key).getContent();
        logger.trace("Loading dependency {} for {}\n{}", key, context.getDependentTemplate(), result);
        return result;
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
