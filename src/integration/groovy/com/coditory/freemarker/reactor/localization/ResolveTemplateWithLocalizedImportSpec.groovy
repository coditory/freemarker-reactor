package com.coditory.freemarker.reactor.localization

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveTemplateWithLocalizedImportSpec extends Specification implements ProcessesTemplate {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("localization/localized-import"))
            .build()

    def "should resolve template with non localized import when no locale is passed"() {
        when:
            String result = processTemplate("greetings")
        then:
            result == multiline(
                    "Template: greetings",
                    "Hello John from template: a\n"
            )
    }

    def "should resolve non-localized template when localized is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("pl", "PL"))
        then:
            result == multiline(
                    "Template: greetings",
                    "Hello John from template: a\n"
            )
    }

    def "should resolve template localized by language and region if possible"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "US"))
        then:
            result == multiline(
                    "Template: greetings",
                    "Hello John from template: a_en_US\n"
            )
    }

    def "should resolve template localized by language only when better match is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "GB"))
        then:
            result == multiline(
                    "Template: greetings",
                    "Hello John from template: a_en\n"
            )
    }
}
