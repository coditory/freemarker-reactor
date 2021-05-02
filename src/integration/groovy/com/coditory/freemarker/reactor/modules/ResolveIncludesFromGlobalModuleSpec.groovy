package com.coditory.freemarker.reactor.modules

import com.coditory.freemarker.reactor.Template
import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveIncludesFromGlobalModuleSpec extends Specification implements ProcessesTemplate {
    TemplateFactory engine = TemplateFactory.builder()
            .setCommonModules(["common"])
            .setTemplateLoader(new ClasspathTemplateLoader("modules/common-module"))
            .build()

    def "should resolve missing includes from global common module"() {
        given:
            Template template = createTemplate(["ipsum"], "ipsum")
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template: ipsum/ipsum",
                    "Template: common/footer"
            )
    }

    def "should resolve includes from local module"() {
        given:
            Template template = createTemplate(["lorem"], "lorem")
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template: lorem/lorem",
                    "Template: lorem/footer"
            )
    }
}
