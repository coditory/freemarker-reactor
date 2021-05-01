package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveTemplateWithNotParsedInclude extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("includes/not-parsed-includes"))
            .build()

    def "should resolve template with not parsed include"() {
        when:
            String result = processTemplate("test")
        then:
            result == multiline(
                    "Template: raw-includes",
                    "Template: a",
                    "Value: \${5+5}",
                    "Template: b",
                    "Template: a",
                    "Value: 10\n",
            )
    }

}
