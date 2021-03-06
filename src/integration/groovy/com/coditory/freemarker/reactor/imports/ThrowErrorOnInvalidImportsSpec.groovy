package com.coditory.freemarker.reactor.imports

import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.TemplateResolutionException
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

class ThrowErrorOnInvalidImportsSpec extends Specification implements ProcessesTemplate {
    TemplateFactory engine = TemplateFactory.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("imports/invalid-imports"))
            .build()

    def "should throw error on missing import"() {
        when:
            processTemplate("missing-import")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-import'"
            e.cause.message.startsWith("Missing template to import: 'missing-123'")
    }

    def "should throw error on missing transitive import"() {
        when:
            processTemplate("missing-transitive-import")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'missing-transitive-import'"
            e.cause.message.startsWith("Missing template to import: 'missing-123'")
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

    def "should throw error on original import directive"() {
        when:
            processTemplate("original-import")
        then:
            TemplateResolutionException e = thrown(TemplateResolutionException)
            e.message == "Could not resolve template 'original-import'"
            e.cause.message == "Template dependency not recognized. Use reactive directives <@include ...>, <@import ...> instead of synchronous <#include ...>, <#import ...>"
    }
}
