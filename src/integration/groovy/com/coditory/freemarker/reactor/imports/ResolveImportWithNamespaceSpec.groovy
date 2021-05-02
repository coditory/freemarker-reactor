package com.coditory.freemarker.reactor.imports

import com.coditory.freemarker.reactor.TemplateFactory
import com.coditory.freemarker.reactor.base.ProcessesTemplate
import com.coditory.freemarker.reactor.loader.ClasspathTemplateLoader
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveImportWithNamespaceSpec extends Specification implements ProcessesTemplate {
    TemplateFactory engine = TemplateFactory.builder()
            .setTemplateLoader(new ClasspathTemplateLoader("imports/namespaces"))
            .build()

    def "should resolve imports with a namespace"() {
        when:
            String result = processTemplate("import-with-namespace")
        then:
            result == multiline(
                    "Template: import-with-namespace",
                    "Hello John from template: a",
                    "Hello Jack from template: b",
                    "Hello James from template: c\n"
            )
    }

    def "should resolve imports with a conflicting namespace"() {
        when:
            String result = processTemplate("import-with-conflicting-namespace")
        then:
            result == multiline(
                    "Template: import-with-conflicting-namespace",
                    "Hello John from template: b\n"
            )
    }

    def "should use proper default namespace for directory imports"() {
        when:
            String result = processTemplate("c/d/namespace-for-dir-imports")
        then:
            result == multiline(
                    "Template: namespace-for-dir-imports",
                    "Hello John from template: c",
                    "Hello Jack from template: c/d/_index\n"
            )
    }

    def "should use proper default namespace for _index imports"() {
        when:
            String result = processTemplate("c/d/namespace-for-index-imports")
        then:
            result == multiline(
                    "Template: namespace-for-index-imports",
                    "Hello Jack from template: c/d/_index\n"
            )
    }

    def "should use proper default namespace for protected sibling"() {
        when:
            String result = processTemplate("c/d/namespace-for-protected-sibling")
        then:
            result == multiline(
                    "Template: namespace-for-protected-sibling",
                    "Hello John from template: c/d/_sibling\n"
            )
    }
}
