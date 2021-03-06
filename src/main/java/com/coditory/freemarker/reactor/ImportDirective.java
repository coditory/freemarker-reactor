package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
import freemarker.core._MiscTemplateException;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.coditory.freemarker.reactor.TemplateConstants.PROTECTED_TEMPLATE_PREFIX;
import static com.coditory.freemarker.reactor.TemplateConstants.SEPARATOR;
import static java.util.Objects.requireNonNull;

final class ImportDirective implements TemplateDirective {
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
        TemplateKey importKey = getImport(currentTemplateKey, env, params, positional);
        String importNameSpace = getImportNameSpace(importKey, params, positional);
        context.addDependency(currentTemplateKey, importKey);
        if (context.isMissing(importKey)) {
            throw new _MiscTemplateException(env, "Missing template to import: " + importKey);
        }
        if (context.isResolved(importKey)) {
            try {
                TemplateKey parent = context.getParentTemplate();
                context.setParentTemplate(currentTemplateKey);
                env.importLib(importKey.getName(), importNameSpace);
                logger.debug("Imported template {} as '{}' into {}", importKey, importNameSpace, currentTemplateKey);
                context.setParentTemplate(parent);
            } catch (IOException e) {
                throw new _MiscTemplateException(
                        e, env,
                        "Could not import template: " + importKey + ":\n",
                        e.getMessage()
                );
            }
        }
    }

    private TemplateKey getImport(
            TemplateKey template,
            Environment env,
            Map<String, TemplateModel> params,
            List<TemplateModel> positional
    ) throws TemplateException {
        String param = getImportParam(env, params, positional);
        return template.dependencyKey(param);
    }

    private String getImportParam(
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
                    "Could not execute import directive parameters: Missing template name to import"
            );
        }
        return positional.get(0).toString();
    }

    private String getImportNameSpace(
            TemplateKey templateKey,
            Map<String, TemplateModel> params,
            List<TemplateModel> positional
    ) {
        TemplateModel namedParam = params.get("ns");
        if (namedParam != null) {
            return namedParam.toString();
        }
        String value = positional.size() < 2 || positional.get(1) == null
                ? ""
                : positional.get(1).toString();
        return value == null || value.isBlank()
                ? getNamespaceFromTemplateName(templateKey)
                : value;
    }

    private String getNamespaceFromTemplateName(TemplateKey templateKey) {
        String[] parts = templateKey.getTemplateBaseName().split(SEPARATOR);
        String last = parts[parts.length - 1];
        return last.startsWith(PROTECTED_TEMPLATE_PREFIX)
                ? last.substring(1)
                : last;
    }
}
