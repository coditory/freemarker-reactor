package com.coditory.freemarker.reactor;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class ParametersResolver {
    static Mono<Map<String, Object>> resolveParams(Map<String, Object> params) {
        requireNonNull(params);
        Set<Publisher<?>> publishers = new HashSet<>();
        collectPublishers(params, publishers);
        List<Publisher<?>> orderedPublishers = new ArrayList<>(publishers);
        return Flux.fromIterable(orderedPublishers)
                .flatMapSequential(p -> p instanceof Flux
                        ? ((Flux<?>) p).collectList()
                        : p
                )
                .collectList()
                .map(resolved -> {
                    Map<Publisher<?>, Object> resolvedPublishers = new HashMap<>();
                    for (int i = 0; i < resolved.size(); ++i) {
                        resolvedPublishers.put(orderedPublishers.get(i), resolved.get(i));
                    }
                    return resolvePublishers(params, resolvedPublishers);
                });
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> resolvePublishers(Map<String, Object> params, Map<Publisher<?>, Object> resolved) {
        return params.entrySet().stream()
                .map(entry -> {
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        Object mapped = resolvePublishers((Map<String, Object>) value, resolved);
                        return Map.entry(entry.getKey(), mapped);
                    }
                    if (value instanceof Publisher && resolved.containsKey(value)) {
                        return Map.entry(entry.getKey(), resolved.get(value));
                    }
                    return entry;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SuppressWarnings("unchecked")
    private static void collectPublishers(Map<String, Object> params, Set<Publisher<?>> publishers) {
        params.values()
                .forEach(value -> {
                    if (value instanceof Map) {
                        collectPublishers((Map<String, Object>) value, publishers);
                    } else if (value instanceof Publisher) {
                        publishers.add((Publisher<?>) value);
                    }
                });
    }
}
