package com.coditory.freemarker.reactor.imports

import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveImportsFromRelativePathsSpec extends Specification implements ProcessesTemplate {
    TemplateFactory engine = TemplateFactory.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("imports/relative-path-imports"))
            .build()

    def "should resolve imports from relative paths"() {
        when:
            String result = processTemplate("x/test")
        then:
            result == multiline(
                    "Template with relative imports",
                    "Macro: a",
                    "Macro: b",
                    "Macro: c",
                    "Macro: b\n"
            )
    }
}
