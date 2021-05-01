package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveOptionalIncludesSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("includes/optional-includes"))
            .build()

    def "should resolve template with existing optional include"() {
        when:
            String result = processTemplate("existing-include")
        then:
            result == multiline(
                    "Template: existing-include",
                    "Template: a"
            )
    }

    def "should resolve template with non-existing optional include"() {
        when:
            String result = processTemplate("missing-include")
        then:
            result == multiline(
                    "Template: missing-include\n"
            )
    }

    def "should resolve template with non-existing transitive optional include"() {
        when:
            String result = processTemplate("missing-transitive-include")
        then:
            result == multiline(
                    "Template: missing-transitive-include",
                    "Template: missing-include\n"
            )
    }
}
