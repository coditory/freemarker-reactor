package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.CachedTemplateLoader;
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader;
import com.coditory.freemarker.reactor.loader.TemplateLoader;
import freemarker.cache.NullCacheStorage;
import freemarker.core.ArithmeticEngine;
import freemarker.core.OutputFormat;
import freemarker.core.TemplateClassResolver;
import freemarker.core.TemplateDateFormatFactory;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.TruncateBuiltinAlgorithm;
import freemarker.template.AttemptExceptionReporter;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static java.util.Objects.requireNonNull;

public final class TemplateFactoryBuilder {
    private final Configuration configuration;
    private Locale defaultLocale;
    private List<String> commonModules = List.of();
    private TemplateLoader templateLoader = new CachedTemplateLoader(new ClasspathTemplateLoader("templates"));
    private Cache<TemplateKey, ResolvedTemplate> templateResolverCache = Cache.concurrentMapCache();

    TemplateFactoryBuilder(Version version) {
        this.configuration = new Configuration(version);
        this.configuration.setCacheStorage(new NullCacheStorage());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setWhitespaceStripping(true);
        this.configuration.setLogTemplateExceptions(false);
    }

    public TemplateFactoryBuilder setDefaultEncoding(String defaultEncoding) {
        configuration.setDefaultEncoding(defaultEncoding);
        return this;
    }

    public TemplateFactoryBuilder setAutoEscapingPolicy(int autoEscapingPolicy) {
        configuration.setAutoEscapingPolicy(autoEscapingPolicy);
        return this;
    }

    public TemplateFactoryBuilder setAllSharedVariables(TemplateHashModelEx allSharedVariables) throws TemplateModelException {
        configuration.setAllSharedVariables(allSharedVariables);
        return this;
    }

    public TemplateFactoryBuilder setAttemptExceptionReporter(AttemptExceptionReporter attemptExceptionReporter) {
        configuration.setAttemptExceptionReporter(attemptExceptionReporter);
        return this;
    }

    public TemplateFactoryBuilder setFallbackOnNullLoopVariable(boolean fallback) {
        configuration.setFallbackOnNullLoopVariable(fallback);
        return this;
    }

    public TemplateFactoryBuilder setIncompatibleImprovements(Version incompatibleImprovements) {
        configuration.setIncompatibleImprovements(incompatibleImprovements);
        return this;
    }

    public TemplateFactoryBuilder setInterpolationSyntax(int interpolationSyntax) {
        configuration.setInterpolationSyntax(interpolationSyntax);
        return this;
    }

    public TemplateFactoryBuilder setDefaultLocale(Locale locale) {
        this.defaultLocale = locale;
        configuration.setLocale(locale);
        return this;
    }

    public TemplateFactoryBuilder setNamingConvention(int namingConvention) {
        configuration.setNamingConvention(namingConvention);
        return this;
    }

    public TemplateFactoryBuilder setNamingConvention(ObjectWrapper objectWrapper) {
        configuration.setObjectWrapper(objectWrapper);
        return this;
    }

    public TemplateFactoryBuilder setOutputFormat(OutputFormat outputFormat) {
        configuration.setOutputFormat(outputFormat);
        return this;
    }

    public TemplateFactoryBuilder setRegisteredCustomOutputFormats(Collection<? extends OutputFormat> registeredCustomOutputFormats) {
        configuration.setRegisteredCustomOutputFormats(registeredCustomOutputFormats);
        return this;
    }

    public TemplateFactoryBuilder setSetting(String name, String value) throws TemplateException {
        configuration.setSetting(name, value);
        return this;
    }

    public TemplateFactoryBuilder setSetting(String name, Object value) throws TemplateException {
        configuration.setSharedVariable(name, value);
        return this;
    }

    public TemplateFactoryBuilder setSetting(String name, TemplateModel value) throws TemplateException {
        configuration.setSharedVariable(name, value);
        return this;
    }

    public TemplateFactoryBuilder setSharedVariables(Map<String, ?> variables) throws TemplateException {
        configuration.setSharedVariables(variables);
        return this;
    }

    public TemplateFactoryBuilder setSharedVariables(int tabSize) {
        configuration.setTabSize(tabSize);
        return this;
    }

    public TemplateFactoryBuilder setTagSyntax(int tagSyntax) {
        configuration.setTagSyntax(tagSyntax);
        return this;
    }

    public TemplateFactoryBuilder setTimeZone(TimeZone timeZone) {
        configuration.setTimeZone(timeZone);
        return this;
    }

    public TemplateFactoryBuilder setWhitespaceStripping(boolean whitespaceStripping) {
        configuration.setWhitespaceStripping(whitespaceStripping);
        return this;
    }

    public TemplateFactoryBuilder setWrapUncheckedExceptions(boolean wrapUncheckedExceptions) {
        configuration.setWrapUncheckedExceptions(wrapUncheckedExceptions);
        return this;
    }

    public TemplateFactoryBuilder setApiBuiltinEnabled(boolean apiBuiltinEnabled) {
        configuration.setAPIBuiltinEnabled(apiBuiltinEnabled);
        return this;
    }

    public TemplateFactoryBuilder setApiBuiltinEnabled(ArithmeticEngine arithmeticEngine) {
        configuration.setArithmeticEngine(arithmeticEngine);
        return this;
    }

    public TemplateFactoryBuilder setAutoImports(Map<String, String> map) {
        configuration.setAutoImports(map);
        return this;
    }

    public TemplateFactoryBuilder setAutoIncludes(List<String> includes) {
        configuration.setAutoIncludes(includes);
        return this;
    }

    public TemplateFactoryBuilder setBooleanFormat(String booleanFormat) {
        configuration.setBooleanFormat(booleanFormat);
        return this;
    }

    public TemplateFactoryBuilder setAutoIncludes(String booleanFormat) {
        configuration.setBooleanFormat(booleanFormat);
        return this;
    }

    public TemplateFactoryBuilder setClassicCompatible(boolean classicCompatible) {
        configuration.setClassicCompatible(classicCompatible);
        return this;
    }

    public TemplateFactoryBuilder setCustomAttribute(String name, Object value) {
        configuration.setCustomAttribute(name, value);
        return this;
    }

    public TemplateFactoryBuilder setCustomDateFormats(Map<String, ? extends TemplateDateFormatFactory> customDateFormats) {
        configuration.setCustomDateFormats(customDateFormats);
        return this;
    }

    public TemplateFactoryBuilder setCustomNumberFormats(Map<String, ? extends TemplateNumberFormatFactory> customNumberFormats) {
        configuration.setCustomNumberFormats(customNumberFormats);
        return this;
    }

    public TemplateFactoryBuilder setDateFormat(String dateFormat) {
        configuration.setDateFormat(dateFormat);
        return this;
    }

    public TemplateFactoryBuilder setDateTimeFormat(String dateTimeFormat) {
        configuration.setDateTimeFormat(dateTimeFormat);
        return this;
    }

    public TemplateFactoryBuilder setLazyAutoImports(boolean lazyAutoImports) {
        configuration.setLazyAutoImports(lazyAutoImports);
        return this;
    }

    public TemplateFactoryBuilder setNewBuiltinClassResolver(TemplateClassResolver newBuiltinClassResolver) {
        configuration.setNewBuiltinClassResolver(newBuiltinClassResolver);
        return this;
    }

    public TemplateFactoryBuilder setOutputEncoding(String outputEncoding) {
        configuration.setOutputEncoding(outputEncoding);
        return this;
    }

    public TemplateFactoryBuilder setNumberFormat(String numberFormat) {
        configuration.setNumberFormat(numberFormat);
        return this;
    }

    public TemplateFactoryBuilder setShowErrorTips(boolean showErrorTips) {
        configuration.setShowErrorTips(showErrorTips);
        return this;
    }

    public TemplateFactoryBuilder setTimeFormat(String timeFormat) {
        configuration.setTimeFormat(timeFormat);
        return this;
    }

    public TemplateFactoryBuilder setTruncateBuiltinAlgorithm(TruncateBuiltinAlgorithm truncateBuiltinAlgorithm) {
        configuration.setTruncateBuiltinAlgorithm(truncateBuiltinAlgorithm);
        return this;
    }

    public TemplateFactoryBuilder setTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoader = requireNonNull(templateLoader);
        return this;
    }

    public TemplateFactoryBuilder setCommonModules(List<String> commonModules) {
        this.commonModules = requireNonNull(commonModules);
        return this;
    }

    @SuppressWarnings("unchecked")
    public TemplateFactoryBuilder setTemplateCache(Cache<TemplateKey, ?> cache) {
        requireNonNull(cache);
        this.templateResolverCache = (Cache<TemplateKey, ResolvedTemplate>) cache;
        return this;
    }

    public TemplateFactoryBuilder removeCache() {
        this.templateResolverCache = Cache.alwaysEmpty();
        return this;
    }

    public TemplateFactory build() {
        configuration.setSharedVariable("include", new TemplateDirectiveAdapter(new IncludeDirective()));
        configuration.setSharedVariable("import", new TemplateDirectiveAdapter(new ImportDirective()));
        configuration.setTemplateLoader(new FreeMarkerTemplateLoaderAdapter());
        TemplateResolver loader = new TemplateResolver(templateLoader, commonModules, templateResolverCache);
        return new TemplateFactory(configuration, loader, defaultLocale);
    }
}
