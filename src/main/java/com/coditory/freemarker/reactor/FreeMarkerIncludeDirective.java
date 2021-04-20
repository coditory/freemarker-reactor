package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
import freemarker.core._MiscTemplateException;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.util.Map;

final class FreeMarkerIncludeDirective implements TemplateDirectiveModel {
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        String sourceTemplateName = env.getCurrentTemplate().getName();
        String templateName = params.get("name").toString();
        boolean parse = true; //params.get("parse").toString();
        boolean optional = false; //params.get("optional").toString();
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        if (context.isDependencyLoaded(templateName)) {
            Template includedTemplate;
            try {
                includedTemplate = env.getTemplateForInclusion(templateName, null, parse, optional);
            } catch (IOException e) {
                throw new _MiscTemplateException(
                        e, env,
                        "Template inclusion failed (for parameter value ", templateName, "):\n",
                        e.getMessage()
                );
            }
            if (includedTemplate != null) {
                env.include(includedTemplate);
            }
        } else {
            context.addDependency(sourceTemplateName, templateName);
        }
    }
}
