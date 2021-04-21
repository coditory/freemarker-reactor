package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.loader.ReactiveFreeMarkerClasspathLoader
import spock.lang.Specification

class ResolveIncludesFromRelativePathsSpec extends Specification {
    ReactiveFreeMarkerTemplateEngine engine = ReactiveFreeMarkerTemplateEngine.builder()
            .setTemplateLoader(new ReactiveFreeMarkerClasspathLoader("relative-paths"))
            .build()

    def "should resolve template dependencies from relative paths"() {
        given:
            ReactiveFreeMarkerTemplate template = engine.createTemplate("sub/template-with-relative-paths").block()
        when:
            String result = template.process().block()
        then:
            result == "Template: a\nTemplate: b\nTemplate: c"
    }
}
