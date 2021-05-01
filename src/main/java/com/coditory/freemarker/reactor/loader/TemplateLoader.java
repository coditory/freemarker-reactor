package com.coditory.freemarker.reactor.loader;

import com.coditory.freemarker.reactor.TemplateKey;
import reactor.core.publisher.Mono;

public interface TemplateLoader {
    Mono<String> loadTemplate(TemplateKey key);
}
