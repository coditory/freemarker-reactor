package com.coditory.freemarker.reactor.base

import com.coditory.freemarker.reactor.TemplateKey
import com.coditory.freemarker.reactor.loader.TemplateLoader
import reactor.core.publisher.Mono

class InMemoryFreeMarkerTemplateLoader implements TemplateLoader {
    private final Map<TemplateKey, Mono<String>> responses = new HashMap<>()
    private Mono<String> defaultResponse = Mono.empty()
    private int requestCount = 0;

    @Override
    Mono<String> loadTemplate(TemplateKey key) {
        requestCount++
        return responses.getOrDefault(key, defaultResponse)
    }

    void setDefaultResponse(Mono<String> defaultResponse) {
        this.defaultResponse = defaultResponse
    }

    void setResponse(String name, String response) {
        this.responses.put(new TemplateKey(null, name, null), Mono.just(response))
    }

    void setResponse(TemplateKey key, String response) {
        this.responses.put(key, Mono.just(response))
    }

    void setResponse(TemplateKey key, Mono<String> response) {
        this.responses.put(key, response)
    }
}
