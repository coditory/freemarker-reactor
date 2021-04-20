package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

class LoadTemplateFromClasspathSpec extends Specification {
    def "should resolve a template from classpath"() {
        given:
            ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
                    .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader())
                    .build()
        when:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("greetings").block()
        and:
            String result = template.process(name: "John").block()
        then:
            result == "Hello John"
    }
}
