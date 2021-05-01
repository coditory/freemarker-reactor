package com.coditory.freemarker.reactor.modules

import com.coditory.freemarker.reactor.Template
import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveModuleRelativeIncludesSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("modules/module-relative-includes"))
            .build()

    def "should resolve module relative includes"() {
        given:
            Template template = createTemplate(["ipsum"], "ipsum")
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template: ipsum/ipsum",
                    "Template: ipsum/a",
                    "Template: ipsum/b/b"
            )
    }
}
