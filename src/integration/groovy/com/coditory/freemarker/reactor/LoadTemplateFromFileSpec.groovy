package com.coditory.freemarker.reactor


import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerFileLoader
import spock.lang.Specification

import java.nio.file.Path

class LoadTemplateFromFileSpec extends Specification {
    def "should resolve a template from a file"() {
        given:
            ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
                    .setTemplateLoader(new ReactiveFreeMarkerFileLoader(Path.of("src/integration/resources")))
                    .build()
        when:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("greetings").block()
        and:
            String result = template.process(name: "John").block()
        then:
            result == "Hello John"
    }
}
