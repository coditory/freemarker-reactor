package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveTransitiveIncludesSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("includes/transitive-includes"))
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
