package com.coditory.freemarker.reactor.modules

import com.coditory.freemarker.reactor.Template
import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveTransitiveIncludesSpec extends Specification implements ProcessesTemplate {
    TemplateFactory engine = TemplateFactory.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("modules/module-transitive-includes"))
            .build()

    def "should resolve scoped includes from local module"() {
        given:
            Template template = createTemplate(["ipsum", "common2", "common1"], "scoped-includes")
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template: ipsum/scoped-includes.ftl",
                    "Template: ipsum/_c",
                    "Template: common2/scoped-includes2.ftl",
                    "Template: common2/_c",
                    "Template: common2/_b",
                    "Template: common2/_c",
                    "Template: common1/scoped-includes1.ftl",
                    "Template: common1/_c\n"
            )
    }

    def "should resolve non-scoped includes from using module hierarchy"() {
        given:
            Template template = createTemplate(["ipsum", "common2", "common1"], "non-scoped-includes")
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template: ipsum/non-scoped-includes.ftl",
                    "Template: ipsum/c",
                    "Template: common2/b",
                    "Template: common2/_c",
                    "Template: common2/non-scoped-includes2.ftl",
                    "Template: ipsum/c",
                    "Template: common2/b",
                    "Template: common2/_c",
                    "Template: common1/non-scoped-includes1.ftl",
                    "Template: ipsum/c",
                    "Template: common2/b",
                    "Template: common2/_c\n"
            )
    }
}
