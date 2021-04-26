package com.coditory.freemarker.reactor.localization

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplate
import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

class ResolveLocalizedTemplateFromIndexSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("localization/localized-template-index"))
            .build()

    def "should resolve non-localized template from index when no locale is passed"() {
        when:
            String result = resolve()
        then:
            result == "Template: greetings/index"
    }

    def "should resolve non-localized template from index when localized is not available"() {
        when:
            String result = resolve(new Locale("fr", "FR"))
        then:
            result == "Template: greetings/index"
    }

    def "should resolve template from index localized by language and region if possible"() {
        when:
            String result = resolve(new Locale("en", "US"))
        then:
            result == "Template: greetings/index_en_US"
    }

    def "should resolve template from index localized by language only when better match is not available"() {
        when:
            String result = resolve(new Locale("en", "GB"))
        then:
            result == "Template: greetings/index_en"
    }

    def "should resolve template from non index file when both are available"() {
        when:
            String result = resolve(new Locale("pl"))
        then:
            result == "Template: greetings_pl"
    }

    private String resolve(Locale locale = null) {
        ReactiveFreeMarkerTemplate template = engine
                .createTemplate("greetings", locale as Locale)
                .block()
        return template.process().block()
    }
}
