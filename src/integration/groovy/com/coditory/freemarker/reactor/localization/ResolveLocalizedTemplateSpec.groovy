package com.coditory.freemarker.reactor.localization

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplate
import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveLocalizedTemplateSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("localization/localized-template"))
            .build()

    def "should resolve non-localized template when no locale is passed"() {
        when:
            String result = resolve()
        then:
            result == "Template: greetings"
    }

    def "should resolve non-localized template when localized is not available"() {
        when:
            String result = resolve(new Locale("pl", "PL"))
        then:
            result == "Template: greetings"
    }

    def "should resolve template localized by language and region if possible"() {
        when:
            String result = resolve(new Locale("en", "US"))
        then:
            result == "Template: greetings_en_US"
    }

    def "should resolve template localized by language only when better match is not available"() {
        when:
            String result = resolve(new Locale("en", "GB"))
        then:
            result == "Template: greetings_en"
    }

    private String resolve(Locale locale = null) {
        ReactiveFreeMarkerTemplate template = engine
                .createTemplate("greetings", locale as Locale)
                .block()
        return template.process().block()
    }
}
