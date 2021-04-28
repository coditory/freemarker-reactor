package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveTransitiveIncludesSpec extends Specification implements ProcessesTemplate {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("includes/transitive-includes"))
            .build()

    def "should resolve template with one include"() {
        when:
            String result = processTemplate("a", [b: true])
        then:
            result == multiline(
                    "Template: a",
                    "Template: b\n"
            )
    }

    def "should resolve template with two transitive includes"() {
        when:
            String result = processTemplate("a", [b: true, c: true])
        then:
            result == multiline(
                    "Template: a",
                    "Template: b",
                    "Template: c\n"
            )
    }
}
