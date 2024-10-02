package de.insiderpie.htmlvalidatorspider;

public class MIMETypeSyntaxException extends Exception {
    private final String mimeType;

    public MIMETypeSyntaxException(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getMessage() {
        return "Invalid MIME Type syntax: %s".formatted(mimeType);
    }
}
