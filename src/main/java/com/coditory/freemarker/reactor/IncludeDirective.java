package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
import freemarker.core._MiscTemplateException;
import freemarker.ext.beans.BooleanModel;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        TemplateKey templateKey = resolveTemplateKey(env, context);
        TemplateKey includeKey = getInclude(templateKey, env, params, positional);
        boolean parse = getNamedBooleanParamOrTrue(env, params, "parse");
        boolean optional = getNamedBooleanParamOrTrue(env, params, "required");
        context.addDependency(templateKey, includeKey);
        if (context.isLoaded(includeKey)) {
            Template includedTemplate;
            try {
                TemplateKey parent = context.getResolvedTemplate();
                context.setResolvedTemplate(templateKey);
                logger.debug("Including template to {}: {}", templateKey.getName(), includeKey.getName());
                includedTemplate = env.getTemplateForInclusion(includeKey.getName(), null, parse, optional);
                context.setResolvedTemplate(parent);
                if (includedTemplate != null) {
                    env.include(includedTemplate);
                }
            } catch (IOException e) {
                throw new _MiscTemplateException(
                        e, env,
                        "Could not include template:" + includeKey.getName() + ":\n",
                        e.getMessage()
                );
            }
        }
    }

    private TemplateKey resolveTemplateKey(Environment env, TemplateResolutionContext context) {
        String sourceTemplateName = env.getCurrentTemplate().getName();
        TemplateKey parent = context.getResolvedTemplate();
        return context.isLoaded(parent.withName(sourceTemplateName))
                ? context.getResolvedTemplate(parent.withName(sourceTemplateName))
                : parent.withName(sourceTemplateName);
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
        if (!(model instanceof BooleanModel)) {
            throw new _MiscTemplateException(
                    env,
                    "Could not include directive parameter: " + name + ". Expected boolean value, got: " + model
            );
        }
        BooleanModel booleanModel = (BooleanModel) model;
        return booleanModel.getAsBoolean();
    }
}
