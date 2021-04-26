package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveIncludesFromBasePathsSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("base-path-includes"))
            .build()

    def "should resolve template dependencies from base path"() {
        given:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("x/test").block()
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template with base path includes",
                    "Template: a",
                    "Template: b",
                    "Template: c\n"
            )
    }
}
