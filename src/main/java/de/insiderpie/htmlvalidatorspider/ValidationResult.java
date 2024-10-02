package de.insiderpie.htmlvalidatorspider;

import org.xml.sax.SAXParseException;

import java.net.URI;
import java.util.List;

public class ValidationResult {
    private final URI uri;
    private final boolean ok;
    private final String message;

    private ValidationResult(URI uri, boolean ok, String message) {
        this.uri = uri;
        this.ok = ok;
        this.message = message;
    }

    public static ValidationResult ok(URI uri) {
        return new ValidationResult(uri, true, "");
    }

    public static ValidationResult fromException(URI uri, Exception exception) {
        return new ValidationResult(uri, false, exception.getMessage());
    }

    public static ValidationResult fromSAXParseErrors(URI uri, List<SAXParseException> parseErrors) {
        if (parseErrors.isEmpty()) {
            return ok(uri);
        }
        StringBuilder message = new StringBuilder();
        for (SAXParseException error : parseErrors) {
            message.append("L%s:%s \t%s%n".formatted(error.getLineNumber(), error.getColumnNumber(), error.getMessage()));
        }
        return new ValidationResult(uri, false, message.toString());
    }

    public URI getUri() {
        return uri;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
