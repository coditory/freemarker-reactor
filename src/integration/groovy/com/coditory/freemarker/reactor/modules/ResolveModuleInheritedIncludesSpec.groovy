package com.coditory.freemarker.reactor.modules

import com.coditory.freemarker.reactor.Template
import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveModuleInheritedIncludesSpec extends Specification implements ProcessesTemplate {
    TemplateFactory engine = TemplateFactory.builder()
            .setCommonModules(["common2", "common1"])
            .setTemplateLoader(new ClasspathTemplateLoader("modules/module-inheritance"))
            .build()

    def "should resolve module inherited includes"() {
        given:
            Template template = createTemplate(["ipsum"], "ipsum")
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template: common1/header",
                    "Template: ipsum",
                    "Template: common2/footer\n"
            )
    }
}
