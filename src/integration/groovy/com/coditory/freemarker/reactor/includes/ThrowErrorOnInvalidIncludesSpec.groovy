package com.coditory.freemarker.reactor.includes

import com.coditory.freemarker.reactor.ReactiveFreeMarkerTemplateEngine
import com.coditory.freemarker.reactor.TemplateResolutionException
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

class ThrowErrorOnInvalidIncludesSpec extends Specification implements ProcessesTemplate {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("includes/invalid-includes"))
            .build()

    def "should throw error on missing include"() {
        when:
            processTemplate("missing-include")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-include'. Could not resolve template dependency 'missing-123'"
            e.cause.message == "Missing template 'missing-123'"
    }

    def "should throw error on missing transitive include"() {
        when:
            processTemplate("missing-transitive-include")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-transitive-include'. Could not resolve template dependency 'missing-123'"
            e.cause.message == "Missing template 'missing-123'"
    }

    def "should throw error on include from root directory"() {
        when:
            processTemplate("include-from-root-directory")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'include-from-root-directory'"
            e.cause.message == "Template name '/abc' points outside of the base path"
    }

    def "should throw error on include from outside base path"() {
        when:
            processTemplate("include-outside-base-path")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'include-outside-base-path'"
            e.cause.message == "Template name '../a' points outside of the base path"
    }

    def "should throw error on include from outside base path in the middle of the path"() {
        when:
            processTemplate("include-outside-base-path-middle")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'include-outside-base-path-middle'"
            e.cause.message == "Template name 'x/y/../../../a' points outside of the base path"
    }

    def "should throw error on includes cycle"() {
        when:
            processTemplate("cycle/include-with-cycle")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'cycle/include-with-cycle'"
            e.cause.message == "Detected circular template dependency: 'cycle/c' <-> 'cycle/a'"
    }

    def "should throw error on include to itself"() {
        when:
            processTemplate("include-self")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'include-self'"
            e.cause.message == "Detected circular template dependency: 'include-self' <-> 'include-self'"
    }

    def "should throw error on original include macro"() {
        when:
            processTemplate("original-include")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'original-include'"
            e.cause.message == "Template dependency not recognized. Use reactive directives <@include ...>, <@import ...> instead of synchronous <#include ...>, <#import ...>"
    }
}
