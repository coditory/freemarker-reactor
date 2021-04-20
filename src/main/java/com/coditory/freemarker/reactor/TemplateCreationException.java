package com.coditory.freemarker.reactor;

public class TemplateCreationException extends RuntimeException {
    public TemplateCreationException() {
    }

    public TemplateCreationException(String message) {
        super(message);
    }

    public TemplateCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateCreationException(Throwable cause) {
        super(cause);
    }
}
