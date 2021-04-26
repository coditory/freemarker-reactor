package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

class ThrowErrorOnInvalidIncludesSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("invalid-includes"))
            .build()

    def "should throw error on missing include"() {
        when:
            resolve("missing-include")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-include'. Could not resolve template dependency 'missing-123'"
            e.cause.message == "Missing template: 'missing-123'"
    }

    def "should throw error on missing transitive include"() {
        when:
            resolve("missing-transitive-include")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-transitive-include'. Could not resolve template dependency 'missing-123'"
            e.cause.message == "Missing template: 'missing-123'"
    }

    def "should throw error on include from root directory"() {
        when:
            resolve("include-from-root-directory")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template: 'include-from-root-directory'"
            e.cause.message == "Template name points outside base path: '/abc'"
    }

    def "should throw error on include from outside base path"() {
        when:
            resolve("include-outside-base-path")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template: 'include-outside-base-path'"
            e.cause.message == "Template name points outside base path: '../a'"
    }

    def "should throw error on include from outside base path in the middle of the path"() {
        when:
            resolve("include-outside-base-path-middle")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template: 'include-outside-base-path-middle'"
            e.cause.message == "Template name points outside base path: 'x/y/../../../a'"
    }

    def "should throw error on includes cycle"() {
        when:
            resolve("cycle/include-with-cycle")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template: 'cycle/include-with-cycle'"
            e.cause.message == "Detected circular template dependency: cycle/c <-> cycle/a"
    }

    def "should throw error on include to itself"() {
        when:
            resolve("include-self")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template: 'include-self'"
            e.cause.message == "Detected circular template dependency: include-self <-> include-self"
    }

    private void resolve(String templateName) {
        ReactiveFreeMarkerTemplate template = engine.createTemplate(templateName).block()
        template.process().block()
    }
}
