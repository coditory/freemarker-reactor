package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolvePackageScopeTemplatesSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("package-scope-templates"))
            .build()

    def "should throw error when including private template from other directory"() {
        given:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("invalid-include").block()
        when:
            template.process().block()
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template: 'invalid-include'"
            e.cause.message == "Detected dependency to package scope template: invalid-include -> x/_a"
    }

    def "should resolve dependencies that does not violate package templates"() {
        given:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("valid-include").block()
        when:
            String result = template.process().block()
        then:
            result == multiline(
                    "Template: valid-include",
                    "Template: x/_index",
                    "Template: x/_a",
                    "Template: x/_b",
                    "Template: _y\n"
            )
    }
}
