package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveIncludesFromRelativePathsSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("includes/relative-path-includes"))
            .build()

    def "should resolve includes from relative paths"() {
        when:
            String result = processTemplate("x/test")
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
