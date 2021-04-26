package com.coditory.freemarker.reactor.base

final class MultilineString {
    static String multiline(String... lines) {
        return lines.join("\n")
    }
}
