package com.coditory.freemarker.reactor.loader;

import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface ReactiveFreeMarkerTemplateLoader {
    Mono<String> loadTemplate(String name, @Nullable Locale locale);
}
