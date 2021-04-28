package com.coditory.freemarker.reactor;

public final class TemplateResolutionException extends RuntimeException {
    public TemplateResolutionException() {
    }

    public TemplateResolutionException(String message) {
        super(message);
    }

    public TemplateResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateResolutionException(Throwable cause) {
        super(cause);
    }
}
