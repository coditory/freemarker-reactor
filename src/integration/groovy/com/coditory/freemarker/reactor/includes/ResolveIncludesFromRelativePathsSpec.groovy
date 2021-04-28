package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveIncludesFromRelativePathsSpec extends Specification implements ProcessesTemplate {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("includes/relative-path-includes"))
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
