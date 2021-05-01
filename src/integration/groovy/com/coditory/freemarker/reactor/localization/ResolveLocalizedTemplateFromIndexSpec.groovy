package com.coditory.freemarker.reactor.localization

import com.coditory.freemarker.reactor.TemplateEngine
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

class ResolveLocalizedTemplateFromIndexSpec extends Specification implements ProcessesTemplate {
    TemplateEngine engine = TemplateEngine.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("localization/localized-template-index"))
            .build()

    def "should resolve non-localized template from index when no locale is passed"() {
        when:
            String result = processTemplate("greetings")
        then:
            result == "Template: greetings/index"
    }

    def "should resolve non-localized template from index when localized is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("fr", "FR"))
        then:
            result == "Template: greetings/index"
    }

    def "should resolve template from index localized by language and region if possible"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "US"))
        then:
            result == "Template: greetings/index_en_US"
    }

    def "should resolve template from index localized by language only when better match is not available"() {
        when:
            String result = processTemplate("greetings", new Locale("en", "GB"))
        then:
            result == "Template: greetings/index_en"
    }

    def "should resolve template from non index file when both are available"() {
        when:
            String result = processTemplate("greetings", new Locale("pl"))
        then:
            result == "Template: greetings_pl"
    }
}
