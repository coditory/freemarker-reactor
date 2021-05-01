package com.coditory.freemarker.reactor.localization

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

class ResolveLocalizedTemplateSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("localization/localized-template"))
            .build()

    def "should resolve non-localized template when no locale is passed"() {
        when:
            String result = processTemplate("greetings")
        then:
            result == "Template: greetings"
    }

    def "should resolve non-localized template when localized is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("pl", "PL"))
        then:
            result == "Template: greetings"
    }

    def "should resolve template localized by language and region if possible"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "US"))
        then:
            result == "Template: greetings_en_US"
    }

    def "should resolve template localized by language only when better match is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "GB"))
        then:
            result == "Template: greetings_en"
    }
}
