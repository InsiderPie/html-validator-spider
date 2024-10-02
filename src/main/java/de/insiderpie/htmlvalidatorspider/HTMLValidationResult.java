package de.insiderpie.htmlvalidatorspider;

public class HTMLValidationResult {
    private Exception exception;
    private boolean wasValidated;
    private boolean ok;
    private String message;



    public boolean couldValidate() {
        return wasValidated;
    }

    public boolean isOk() {
        return ok;
    }

    public Exception getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }
}
