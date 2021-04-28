package com.coditory.freemarker.reactor.imports

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveImportsFromBasePathsSpec extends Specification implements ProcessesTemplate {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("imports/base-path-imports"))
            .build()

    def "should resolve imports from base path"() {
        when:
            String result = processTemplate("x/test")
        then:
            result == multiline(
                    "Template with base path imports",
                    "Macro: a",
                    "Macro: b",
                    "Macro: c\n"
            )
    }
}
