package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static freemarker.core.TemplatePositionalParametersExtractor.extractPositionalParams;
import static java.util.Objects.requireNonNull;

final class TemplateDirectiveAdapter implements TemplateDirectiveModel {
    private final TemplateDirective directive;

    TemplateDirectiveAdapter(TemplateDirective directive) {
        this.directive = requireNonNull(directive);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes", "deprecated"})
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        List<TemplateModel> positional = extractPositionalParams(env);
        directive.execute(env, params, positional, loopVars, body);
    }
}
