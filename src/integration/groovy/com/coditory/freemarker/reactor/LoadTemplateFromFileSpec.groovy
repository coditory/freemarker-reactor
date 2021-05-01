package com.coditory.freemarker.reactor


import com.coditory.freemarker.reactor.loader.FileTemplateLoader
import spock.lang.Specification

import java.nio.file.Path

class LoadTemplateFromFileSpec extends Specification {
    def "should resolve a template from a file"() {
        given:
            TemplateEngine engine = TemplateEngine.builder()
                    .setTemplateLoader(new FileTemplateLoader(Path.of("src/integration/resources")))
                    .build()
        when:
            Template template = engine.createTemplate("greetings").block()
        and:
            String result = template.process(name: "John").block()
        then:
            result == "Hello John"
    }
}
