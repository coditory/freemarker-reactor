package com.coditory.freemarker.reactor.imports

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.TemplateResolutionException
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

class ThrowErrorOnInvalidImportsSpec extends Specification implements ProcessesTemplate {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("imports/invalid-imports"))
            .build()

    def "should throw error on missing import"() {
        when:
            processTemplate("missing-import")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-import'. Could not resolve template dependency 'missing-123'"
            e.cause.message == "Missing template 'missing-123'"
    }

    def "should throw error on missing transitive import"() {
        when:
            processTemplate("missing-transitive-import")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-transitive-import'. Could not resolve template dependency 'missing-123'"
            e.cause.message == "Missing template 'missing-123'"
    }

    def "should throw error on import from root directory"() {
        when:
            processTemplate("import-from-root-directory")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'import-from-root-directory'"
            e.cause.message == "Template name '/abc' points outside of the base path"
    }

    def "should throw error on include from outside base path"() {
        when:
            processTemplate("import-outside-base-path")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'import-outside-base-path'"
            e.cause.message == "Template name '../a' points outside of the base path"
    }

    def "should throw error on include from outside base path in the middle of the path"() {
        when:
            processTemplate("import-outside-base-path-middle")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'import-outside-base-path-middle'"
            e.cause.message == "Template name 'x/y/../../../a' points outside of the base path"
    }

    def "should throw error on includes cycle"() {
        when:
            processTemplate("cycle/import-with-cycle")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'cycle/import-with-cycle'"
            e.cause.message == "Detected circular template dependency: 'cycle/c' <-> 'cycle/a'"
    }

    def "should throw error on include to itself"() {
        when:
            processTemplate("import-self")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'import-self'"
            e.cause.message == "Detected circular template dependency: 'import-self' <-> 'import-self'"
    }
}
