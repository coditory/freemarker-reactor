package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
import freemarker.core._MiscTemplateException;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

final class ImportDirective implements TemplateDirectiveModel {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException {
        String sourceTemplateName = env.getCurrentTemplate().getName();
        String templateName = params.get("name").toString();
        boolean lazy = false;
        TemplateResolutionContext context = TemplateResolutionContext.getFromThreadLocal();
        TemplateKey parent = context.getResolvedTemplate();
        TemplateKey templateKey = context.isLoaded(parent.withName(sourceTemplateName))
                ? context.getResolvedTemplate(parent.withName(sourceTemplateName))
                : parent.withName(sourceTemplateName);
        TemplateKey dependency = templateKey.dependencyKey(templateName);
        String ns = params.containsKey("ns")
                ? params.get("ns").toString()
                : getNamespaceFromTemplateName(dependency);
        context.validateDependency(templateKey, dependency);
        if (context.isLoaded(dependency)) {
            try {
                context.setResolvedTemplate(templateKey);
                logger.debug("Importing template to {}: {} as {}", templateKey.getName(), dependency.getName(), ns);
                env.importLib(dependency.getName(), ns, lazy);
                context.setResolvedTemplate(parent);
            } catch (IOException e) {
                throw new _MiscTemplateException(
                        e, env,
                        "Template importing failed (for parameter value ", templateName, "):\n",
                        e.getMessage()
                );
            }
        } else {
            context.addDependency(templateKey, dependency);
        }
    }

    private String getNamespaceFromTemplateName(TemplateKey templateKey) {
        String[] parts = templateKey.getTemplateBaseName().split("/");
        String last = parts[parts.length - 1];
        return last.startsWith("_")
                ? last.substring(1)
                : last;
    }
}
