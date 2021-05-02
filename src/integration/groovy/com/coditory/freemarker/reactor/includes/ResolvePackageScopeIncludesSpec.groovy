package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.TemplateResolutionException
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolvePackageScopeIncludesSpec extends Specification implements ProcessesTemplate {
    TemplateFactory engine = TemplateFactory.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("includes/package-scope-includes"))
            .build()

    def "should throw error when including private template from other directory"() {
        when:
            processTemplate("invalid-include")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'invalid-include'"
            e.cause.message == "Detected dependency to package scope template: 'invalid-include' -> 'x/_a'"
    }

    def "should resolve package scope include that does not violate the scope"() {
        when:
            String result = processTemplate("valid-include")
        then:
            result == multiline(
                    "Template: valid-include",
                    "Template: x/_index",
                    "Template: x/_a",
                    "Template: x/_b",
                    "Template: _y\n"
            )
    }
}
