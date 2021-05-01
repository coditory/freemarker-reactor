package com.coditory.freemarker.reactor.imports

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.TemplateResolutionException
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolvePackageScopeImportsSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("imports/package-scope-imports"))
            .build()

    def "should throw error when importing private template from other directory"() {
        when:
            processTemplate("invalid-import")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'invalid-import'"
            e.cause.message == "Detected dependency to package scope template: 'invalid-import' -> 'x/_a'"
    }

    def "should resolve package scope importing that does not violate the scope"() {
        when:
            String result = processTemplate("valid-import")
        then:
            result == multiline(
                    "Template: valid-import",
                    "Template: x/_index",
                    "Template: x/_a",
                    "Template: x/_b",
                    "Template: _y\n"
            )
    }
}
