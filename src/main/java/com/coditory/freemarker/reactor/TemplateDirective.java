package com.coditory.freemarker.reactor;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@FunctionalInterface
interface TemplateDirective {
    void execute(
            Environment env,
            Map<String, TemplateModel> params,
            List<TemplateModel> positionalParams,
            TemplateModel[] loopVars,
            TemplateDirectiveBody body
    ) throws TemplateException, IOException;
}
