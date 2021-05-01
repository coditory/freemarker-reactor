package com.coditory.freemarker.reactor;

import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public interface Cache<K, V> {
    static <K, V> Cache<K, V> alwaysEmpty() {
        return (key, loader) -> loader.apply(key);
    }

    static <K, V> Cache<K, V> concurrentMapCache() {
        ConcurrentMap<K, Mono<V>> map = new ConcurrentHashMap<>();
        return map::computeIfAbsent;
    }

    Mono<V> getOrLoad(K key, Function<K, Mono<V>> load);
}
