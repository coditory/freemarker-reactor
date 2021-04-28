package com.coditory.freemarker.reactor;

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader;
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerTemplateLoader;
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

public final class ReactiveFreeMarkerTemplateEngineBuilder {
    private final Configuration configuration;
    private ReactiveFreeMarkerTemplateLoader templateLoader = new ReactiveFreeMarkerClasspathLoader("templates");
    private Locale defaultLocale;

    ReactiveFreeMarkerTemplateEngineBuilder(Version version) {
        this.configuration = new Configuration(version);
        this.configuration.setCacheStorage(new NullCacheStorage());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setWhitespaceStripping(true);
        this.configuration.setLogTemplateExceptions(false);
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setDefaultEncoding(String defaultEncoding) {
        configuration.setDefaultEncoding(defaultEncoding);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setAutoEscapingPolicy(int autoEscapingPolicy) {
        configuration.setAutoEscapingPolicy(autoEscapingPolicy);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setAllSharedVariables(TemplateHashModelEx allSharedVariables) throws TemplateModelException {
        configuration.setAllSharedVariables(allSharedVariables);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setAttemptExceptionReporter(AttemptExceptionReporter attemptExceptionReporter) {
        configuration.setAttemptExceptionReporter(attemptExceptionReporter);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setFallbackOnNullLoopVariable(boolean fallback) {
        configuration.setFallbackOnNullLoopVariable(fallback);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setIncompatibleImprovements(Version incompatibleImprovements) {
        configuration.setIncompatibleImprovements(incompatibleImprovements);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setInterpolationSyntax(int interpolationSyntax) {
        configuration.setInterpolationSyntax(interpolationSyntax);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setDefaultLocale(Locale locale) {
        this.defaultLocale = locale;
        configuration.setLocale(locale);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setNamingConvention(int namingConvention) {
        configuration.setNamingConvention(namingConvention);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setNamingConvention(ObjectWrapper objectWrapper) {
        configuration.setObjectWrapper(objectWrapper);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setOutputFormat(OutputFormat outputFormat) {
        configuration.setOutputFormat(outputFormat);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setRegisteredCustomOutputFormats(Collection<? extends OutputFormat> registeredCustomOutputFormats) {
        configuration.setRegisteredCustomOutputFormats(registeredCustomOutputFormats);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setSetting(String name, String value) throws TemplateException {
        configuration.setSetting(name, value);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setSetting(String name, Object value) throws TemplateException {
        configuration.setSharedVariable(name, value);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setSetting(String name, TemplateModel value) throws TemplateException {
        configuration.setSharedVariable(name, value);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setSharedVariables(Map<String, ?> variables) throws TemplateException {
        configuration.setSharedVariables(variables);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setSharedVariables(int tabSize) {
        configuration.setTabSize(tabSize);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setTagSyntax(int tagSyntax) {
        configuration.setTagSyntax(tagSyntax);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setTimeZone(TimeZone timeZone) {
        configuration.setTimeZone(timeZone);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setWhitespaceStripping(boolean whitespaceStripping) {
        configuration.setWhitespaceStripping(whitespaceStripping);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setWrapUncheckedExceptions(boolean wrapUncheckedExceptions) {
        configuration.setWrapUncheckedExceptions(wrapUncheckedExceptions);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setApiBuiltinEnabled(boolean apiBuiltinEnabled) {
        configuration.setAPIBuiltinEnabled(apiBuiltinEnabled);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setApiBuiltinEnabled(ArithmeticEngine arithmeticEngine) {
        configuration.setArithmeticEngine(arithmeticEngine);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setAutoImports(Map<String, String> map) {
        configuration.setAutoImports(map);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setAutoIncludes(List<String> includes) {
        configuration.setAutoIncludes(includes);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setBooleanFormat(String booleanFormat) {
        configuration.setBooleanFormat(booleanFormat);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setAutoIncludes(String booleanFormat) {
        configuration.setBooleanFormat(booleanFormat);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setClassicCompatible(boolean classicCompatible) {
        configuration.setClassicCompatible(classicCompatible);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setCustomAttribute(String name, Object value) {
        configuration.setCustomAttribute(name, value);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setCustomDateFormats(Map<String, ? extends TemplateDateFormatFactory> customDateFormats) {
        configuration.setCustomDateFormats(customDateFormats);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setCustomNumberFormats(Map<String, ? extends TemplateNumberFormatFactory> customNumberFormats) {
        configuration.setCustomNumberFormats(customNumberFormats);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setDateFormat(String dateFormat) {
        configuration.setDateFormat(dateFormat);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setDateTimeFormat(String dateTimeFormat) {
        configuration.setDateTimeFormat(dateTimeFormat);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setLazyAutoImports(boolean lazyAutoImports) {
        configuration.setLazyAutoImports(lazyAutoImports);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setNewBuiltinClassResolver(TemplateClassResolver newBuiltinClassResolver) {
        configuration.setNewBuiltinClassResolver(newBuiltinClassResolver);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setOutputEncoding(String outputEncoding) {
        configuration.setOutputEncoding(outputEncoding);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setNumberFormat(String numberFormat) {
        configuration.setNumberFormat(numberFormat);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setShowErrorTips(boolean showErrorTips) {
        configuration.setShowErrorTips(showErrorTips);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setTimeFormat(String timeFormat) {
        configuration.setTimeFormat(timeFormat);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setTruncateBuiltinAlgorithm(TruncateBuiltinAlgorithm truncateBuiltinAlgorithm) {
        configuration.setTruncateBuiltinAlgorithm(truncateBuiltinAlgorithm);
        return this;
    }

    public ReactiveFreeMarkerTemplateEngineBuilder setTemplateLoader(ReactiveFreeMarkerTemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
        return this;
    }

    public ReactiveFreeMarkerTemplateEngine build() {
        configuration.setSharedVariable("include", new IncludeDirective());
        configuration.setSharedVariable("import", new ImportDirective());
        configuration.setTemplateLoader(new TemplateLoaderAdapter());
        TemplateLoader loader = new TemplateLoader(this.templateLoader, List.of());
        return new ReactiveFreeMarkerTemplateEngine(configuration, loader, defaultLocale);
    }
}
