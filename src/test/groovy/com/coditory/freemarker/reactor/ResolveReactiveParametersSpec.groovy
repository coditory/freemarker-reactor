package com.coditory.freemarker.reactor

import com.coditory.freemarker.reactor.base.InMemoryFreeMarkerTemplateLoader
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import static com.coditory.freemarker.reactor.base.MultilineString.multiline

class ResolveReactiveParametersSpec extends Specification {
    InMemoryFreeMarkerTemplateLoader templateLoader = new InMemoryFreeMarkerTemplateLoader()
    TemplateFactory engine = TemplateFactory.builder()
            .setTemplateLoader(templateLoader)
            .build()

    def "should resolve reactive parameters"() {
        given:
            Map<String, Object> params = [
                    resolved: "Resolved Param",
                    mono    : Mono.just("Mono Param"),
                    flux    : Flux.fromIterable(["Flux", "Param"])
            ]
        when:
            String result = resolveTemplate([
                    "resolved: \${resolved}",
                    "mono: \${mono}",
                    "flux: \${flux[0]}"
            ], params)
        then:
            result == multiline(
                    "resolved: Resolved Param",
                    "mono: Mono Param",
                    "flux: Flux"
            )
    }

    def "should resolve nested reactive parameters"() {
        given:
            Map<String, Object> params = [
                    nested  : [
                            resolved: "Nested Resolved Param",
                            mono    : Mono.just("Nested Mono Param"),
                            flux    : Flux.fromIterable(["Nested", "Flux", "Param"]),
                    ]
            ]
        when:
            String result = resolveTemplate([
                    "nested.resolved: \${nested.resolved}",
                    "nested.mono: \${nested.mono}",
                    "nested.flux: \${nested.flux[0]}"
            ], params)
        then:
            result == multiline(
                    "nested.resolved: Nested Resolved Param",
                    "nested.mono: Nested Mono Param",
                    "nested.flux: Nested"
            )
    }

    private String resolveTemplate(List<String> templateContent, Map<String, Object> params = [:]) {
        String key = "sampleTemplate"
        String content = templateContent.join("\n")
        templateLoader.setResponse(key, content)
        Template template = engine.createTemplate(key)
                .block()
        return template.process(params)
                .block()
    }
}
