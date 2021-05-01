package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
import freemarker.core._MiscTemplateException;
import freemarker.template.Template;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class IncludeDirective implements TemplateDirective {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(
            Environment env,
            Map<String, TemplateModel> params,
            List<TemplateModel> positional,
            TemplateModel[] loopVars,
            TemplateDirectiveBody body
    ) throws TemplateException {
        requireNonNull(env);
        requireNonNull(params);
        requireNonNull(positional);
        requireNonNull(loopVars);
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        TemplateKey currentTemplateKey = context.getCurrentTemplate(env);
        TemplateKey includeKey = getInclude(currentTemplateKey, env, params, positional);
        boolean parse = getNamedBooleanParamOrTrue(env, params, "parse");
        boolean required = getNamedBooleanParamOrTrue(env, params, "required");
        context.addDependency(currentTemplateKey, includeKey);
        if (required && context.isMissing(includeKey)) {
            throw new _MiscTemplateException(env, "Missing template to include: " + includeKey);
        }
        if (context.isResolved(includeKey)) {
            Template includedTemplate;
            try {
                TemplateKey parent = context.getParentTemplate();
                context.setParentTemplate(currentTemplateKey);
                includedTemplate = env.getTemplateForInclusion(includeKey.getName(), null, parse, true);
                if (includedTemplate != null) {
                    env.include(includedTemplate);
                }
                logger.debug("Included template {} into {}", includeKey, currentTemplateKey);
                context.setParentTemplate(parent);
            } catch (IOException e) {
                throw new _MiscTemplateException(
                        e, env,
                        "Could not include template:" + includeKey + ":\n",
                        e.getMessage()
                );
            }
        }
    }

    private TemplateKey getInclude(
            TemplateKey template,
            Environment env,
            Map<String, TemplateModel> params,
            List<TemplateModel> positional
    ) throws TemplateException {
        String param = getNameParam(env, params, positional);
        return template.dependencyKey(param);
    }

    private String getNameParam(
            Environment env,
            Map<String, TemplateModel> params,
            List<TemplateModel> positional
    ) throws TemplateException {
        TemplateModel namedParam = params.get("name");
        if (namedParam != null) {
            return namedParam.toString();
        }
        if (positional.isEmpty() || positional.get(0) == null) {
            throw new _MiscTemplateException(
                    env,
                    "Could not execute include directive parameters: Missing template name to include"
            );
        }
        return positional.get(0).toString();
    }

    private boolean getNamedBooleanParamOrTrue(
            Environment env,
            Map<String, TemplateModel> params,
            String name
    ) throws TemplateException {
        TemplateModel model = params.get(name);
        if (model == null) {
            return true;
        }
        if (!(model instanceof TemplateBooleanModel)) {
            throw new _MiscTemplateException(
                    env,
                    "Could not include directive parameter: " + name + ". Expected boolean value, got: " + model
            );
        }
        TemplateBooleanModel booleanModel = (TemplateBooleanModel) model;
        return booleanModel.getAsBoolean();
    }
}
