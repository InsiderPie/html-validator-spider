package de.insiderpie.htmlvalidatorspider;

public class NotAcceptableException extends Exception {
    private final String contentType;
    private final String accept;

    public NotAcceptableException(String contentType, String accept) {
        this.contentType = contentType;
        this.accept = accept;
    }

    @Override
    public String getMessage() {
        return "Not acceptable: Requested: %s but got: %s".formatted(accept, contentType);
    }
}
