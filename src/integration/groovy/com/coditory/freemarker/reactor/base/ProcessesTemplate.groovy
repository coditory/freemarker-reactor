package com.coditory.freemarker.reactor.base

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplate
import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import groovy.transform.CompileStatic

@CompileStatic
trait ProcessesTemplate {
    abstract ReactiveFreeMarkerTemplateEngine getEngine()

    String processTemplate(String name, Map<String, Object> params = [:]) {
        ReactiveFreeMarkerTemplate template = getEngine()
                .createTemplate(name)
                .block()
        return template.process(params)
                .block()
    }

    String processTemplate(String name, Locale locale, Map<String, Object> params = [:]) {
        ReactiveFreeMarkerTemplate template = getEngine()
            .createTemplate(name, locale)
            .block()
        return template.process(params)
            .block()
    }
}
