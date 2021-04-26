package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveIncludesFromRelativePathsSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("relative-path-includes"))
            .build()

    def "should resolve template dependencies from relative paths"() {
        given:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("x/test").block()
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template with relative includes",
                    "Template: a",
                    "Template: b",
                    "Template: c",
                    "Template: b\n"
            )
    }
}
