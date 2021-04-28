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
        if (!matcher.matches()) {
            return load(name);
        }
        return load(matcher.group(1));
    }

    private String load(String name) {
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        TemplateKey key = context.getResolvedTemplate().withName(name);
        String result = context.getLoaded(key).getContent();
        logger.info("Resolving dependency: {} {} for {}\n{}", name, key, context.getResolvedTemplate(), result);
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
