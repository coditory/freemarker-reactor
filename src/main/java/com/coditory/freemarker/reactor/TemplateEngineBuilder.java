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

public final class TemplateEngineBuilder {
    private final Configuration configuration;
    private Locale defaultLocale;
    private List<String> commonModules = List.of();
    private TemplateLoader templateLoader = new CachedTemplateLoader(new ClasspathTemplateLoader("templates"));
    private Cache<TemplateKey, ResolvedTemplate> templateResolverCache = Cache.concurrentMapCache();

    TemplateEngineBuilder(Version version) {
        this.configuration = new Configuration(version);
        this.configuration.setCacheStorage(new NullCacheStorage());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setWhitespaceStripping(true);
        this.configuration.setLogTemplateExceptions(false);
    }

    public TemplateEngineBuilder setDefaultEncoding(String defaultEncoding) {
        configuration.setDefaultEncoding(defaultEncoding);
        return this;
    }

    public TemplateEngineBuilder setAutoEscapingPolicy(int autoEscapingPolicy) {
        configuration.setAutoEscapingPolicy(autoEscapingPolicy);
        return this;
    }

    public TemplateEngineBuilder setAllSharedVariables(TemplateHashModelEx allSharedVariables) throws TemplateModelException {
        configuration.setAllSharedVariables(allSharedVariables);
        return this;
    }

    public TemplateEngineBuilder setAttemptExceptionReporter(AttemptExceptionReporter attemptExceptionReporter) {
        configuration.setAttemptExceptionReporter(attemptExceptionReporter);
        return this;
    }

    public TemplateEngineBuilder setFallbackOnNullLoopVariable(boolean fallback) {
        configuration.setFallbackOnNullLoopVariable(fallback);
        return this;
    }

    public TemplateEngineBuilder setIncompatibleImprovements(Version incompatibleImprovements) {
        configuration.setIncompatibleImprovements(incompatibleImprovements);
        return this;
    }

    public TemplateEngineBuilder setInterpolationSyntax(int interpolationSyntax) {
        configuration.setInterpolationSyntax(interpolationSyntax);
        return this;
    }

    public TemplateEngineBuilder setDefaultLocale(Locale locale) {
        this.defaultLocale = locale;
        configuration.setLocale(locale);
        return this;
    }

    public TemplateEngineBuilder setNamingConvention(int namingConvention) {
        configuration.setNamingConvention(namingConvention);
        return this;
    }

    public TemplateEngineBuilder setNamingConvention(ObjectWrapper objectWrapper) {
        configuration.setObjectWrapper(objectWrapper);
        return this;
    }

    public TemplateEngineBuilder setOutputFormat(OutputFormat outputFormat) {
        configuration.setOutputFormat(outputFormat);
        return this;
    }

    public TemplateEngineBuilder setRegisteredCustomOutputFormats(Collection<? extends OutputFormat> registeredCustomOutputFormats) {
        configuration.setRegisteredCustomOutputFormats(registeredCustomOutputFormats);
        return this;
    }

    public TemplateEngineBuilder setSetting(String name, String value) throws TemplateException {
        configuration.setSetting(name, value);
        return this;
    }

    public TemplateEngineBuilder setSetting(String name, Object value) throws TemplateException {
        configuration.setSharedVariable(name, value);
        return this;
    }

    public TemplateEngineBuilder setSetting(String name, TemplateModel value) throws TemplateException {
        configuration.setSharedVariable(name, value);
        return this;
    }

    public TemplateEngineBuilder setSharedVariables(Map<String, ?> variables) throws TemplateException {
        configuration.setSharedVariables(variables);
        return this;
    }

    public TemplateEngineBuilder setSharedVariables(int tabSize) {
        configuration.setTabSize(tabSize);
        return this;
    }

    public TemplateEngineBuilder setTagSyntax(int tagSyntax) {
        configuration.setTagSyntax(tagSyntax);
        return this;
    }

    public TemplateEngineBuilder setTimeZone(TimeZone timeZone) {
        configuration.setTimeZone(timeZone);
        return this;
    }

    public TemplateEngineBuilder setWhitespaceStripping(boolean whitespaceStripping) {
        configuration.setWhitespaceStripping(whitespaceStripping);
        return this;
    }

    public TemplateEngineBuilder setWrapUncheckedExceptions(boolean wrapUncheckedExceptions) {
        configuration.setWrapUncheckedExceptions(wrapUncheckedExceptions);
        return this;
    }

    public TemplateEngineBuilder setApiBuiltinEnabled(boolean apiBuiltinEnabled) {
        configuration.setAPIBuiltinEnabled(apiBuiltinEnabled);
        return this;
    }

    public TemplateEngineBuilder setApiBuiltinEnabled(ArithmeticEngine arithmeticEngine) {
        configuration.setArithmeticEngine(arithmeticEngine);
        return this;
    }

    public TemplateEngineBuilder setAutoImports(Map<String, String> map) {
        configuration.setAutoImports(map);
        return this;
    }

    public TemplateEngineBuilder setAutoIncludes(List<String> includes) {
        configuration.setAutoIncludes(includes);
        return this;
    }

    public TemplateEngineBuilder setBooleanFormat(String booleanFormat) {
        configuration.setBooleanFormat(booleanFormat);
        return this;
    }

    public TemplateEngineBuilder setAutoIncludes(String booleanFormat) {
        configuration.setBooleanFormat(booleanFormat);
        return this;
    }

    public TemplateEngineBuilder setClassicCompatible(boolean classicCompatible) {
        configuration.setClassicCompatible(classicCompatible);
        return this;
    }

    public TemplateEngineBuilder setCustomAttribute(String name, Object value) {
        configuration.setCustomAttribute(name, value);
        return this;
    }

    public TemplateEngineBuilder setCustomDateFormats(Map<String, ? extends TemplateDateFormatFactory> customDateFormats) {
        configuration.setCustomDateFormats(customDateFormats);
        return this;
    }

    public TemplateEngineBuilder setCustomNumberFormats(Map<String, ? extends TemplateNumberFormatFactory> customNumberFormats) {
        configuration.setCustomNumberFormats(customNumberFormats);
        return this;
    }

    public TemplateEngineBuilder setDateFormat(String dateFormat) {
        configuration.setDateFormat(dateFormat);
        return this;
    }

    public TemplateEngineBuilder setDateTimeFormat(String dateTimeFormat) {
        configuration.setDateTimeFormat(dateTimeFormat);
        return this;
    }

    public TemplateEngineBuilder setLazyAutoImports(boolean lazyAutoImports) {
        configuration.setLazyAutoImports(lazyAutoImports);
        return this;
    }

    public TemplateEngineBuilder setNewBuiltinClassResolver(TemplateClassResolver newBuiltinClassResolver) {
        configuration.setNewBuiltinClassResolver(newBuiltinClassResolver);
        return this;
    }

    public TemplateEngineBuilder setOutputEncoding(String outputEncoding) {
        configuration.setOutputEncoding(outputEncoding);
        return this;
    }

    public TemplateEngineBuilder setNumberFormat(String numberFormat) {
        configuration.setNumberFormat(numberFormat);
        return this;
    }

    public TemplateEngineBuilder setShowErrorTips(boolean showErrorTips) {
        configuration.setShowErrorTips(showErrorTips);
        return this;
    }

    public TemplateEngineBuilder setTimeFormat(String timeFormat) {
        configuration.setTimeFormat(timeFormat);
        return this;
    }

    public TemplateEngineBuilder setTruncateBuiltinAlgorithm(TruncateBuiltinAlgorithm truncateBuiltinAlgorithm) {
        configuration.setTruncateBuiltinAlgorithm(truncateBuiltinAlgorithm);
        return this;
    }

    public TemplateEngineBuilder setTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoader = requireNonNull(templateLoader);
        return this;
    }

    public TemplateEngineBuilder setCommonModules(List<String> commonModules) {
        this.commonModules = requireNonNull(commonModules);
        return this;
    }

    @SuppressWarnings("unchecked")
    public TemplateEngineBuilder setTemplateCache(Cache<TemplateKey, ?> cache) {
        requireNonNull(cache);
        this.templateResolverCache = (Cache<TemplateKey, ResolvedTemplate>) cache;
        return this;
    }

    public TemplateEngineBuilder removeCache() {
        this.templateResolverCache = Cache.alwaysEmpty();
        return this;
    }

    public TemplateEngine build() {
        configuration.setSharedVariable("include", new TemplateDirectiveAdapter(new IncludeDirective()));
        configuration.setSharedVariable("import", new TemplateDirectiveAdapter(new ImportDirective()));
        configuration.setTemplateLoader(new FreeMarkerTemplateLoaderAdapter());
        TemplateResolver loader = new TemplateResolver(templateLoader, commonModules, templateResolverCache);
        return new TemplateEngine(configuration, loader, defaultLocale);
    }
}
