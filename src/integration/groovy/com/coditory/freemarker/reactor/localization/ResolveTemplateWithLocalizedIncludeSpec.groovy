package com.coditory.freemarker.reactor.localization

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveTemplateWithLocalizedIncludeSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("localization/localized-include"))
            .build()

    def "should resolve template with non localized include when no locale is passed"() {
        when:
            String result = processTemplate("greetings")
        then:
            result == multiline(
                    "Template: greetings",
                    "Template: a"
            )
    }

    def "should resolve non-localized template when localized is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("pl", "PL"))
        then:
            result == multiline(
                    "Template: greetings",
                    "Template: a"
            )
    }

    def "should resolve template localized by language and region if possible"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "US"))
        then:
            result == multiline(
                    "Template: greetings",
                    "Template: a_en_US"
            )
    }

    def "should resolve template localized by language only when better match is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "GB"))
        then:
            result == multiline(
                    "Template: greetings",
                    "Template: a_en"
            )
    }
}
