package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

class LoadTemplateFromClasspathSpec extends Specification {
    def "should resolve a template from classpath"() {
        given:
            TemplateFactory engine = TemplateFactory.builder()
                    .setTemplateLoader(new ClasspathTemplateLoader())
                    .build()
        when:
            Template template = engine.createTemplate("greetings").block()
        and:
            String result = template.process(name: "John").block()
        then:
            result == "Hello John"
    }
}
