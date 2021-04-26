package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveTransitiveIncludesSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("transitive-includes"))
            .build()

    def "should resolve template with one dependency"() {
        given:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("a").block()
        when:
            String result = template.process(b: true).block()
        then:
            result == multiline(
                    "Template: a",
                    "Template: b\n"
            )
    }

    def "should resolve template with two transitive dependencies"() {
        given:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("a").block()
        when:
            String result = template.process(b: true, c: true).block()
        then:
            result == multiline(
                    "Template: a",
                    "Template: b",
                    "Template: c\n"
            )
    }
}
