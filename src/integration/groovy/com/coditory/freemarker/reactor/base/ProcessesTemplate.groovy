package com.coditory.freemarker.reactor.base

import com.coditory.freemarker.reactor.Template
import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.TemplateRequest
import groovy.transform.CompileStatic

@CompileStatic
trait ProcessesTemplate {
    abstract TemplateFactory getEngine()

    Template createTemplate(String name, Locale locale = null) {
        return getEngine()
                .createTemplate(name, locale)
                .block()
    }

    Template createTemplate(List<String> modules, String name) {
        TemplateRequest request = TemplateRequest.builder(name)
                .setModules(modules)
                .build()
        return getEngine()
                .createTemplate(request)
                .block()
    }

    String processTemplate(String name, Map<String, Object> params = [:]) {
        return createTemplate(name)
                .process(params)
                .block()
    }

    String processTemplate(String name, Locale locale, Map<String, Object> params = [:]) {
        return createTemplate(name, locale)
                .process(params)
                .block()
    }
}
