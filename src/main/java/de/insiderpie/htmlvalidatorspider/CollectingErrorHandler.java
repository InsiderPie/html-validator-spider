package de.insiderpie.htmlvalidatorspider;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

public class CollectingErrorHandler implements ErrorHandler {
    private final List<SAXParseException> warningsAndErrors = new ArrayList<>();
    private final List<SAXParseException> errors = new ArrayList<>();

    public List<SAXParseException> getErrors(boolean includeWarnings) {
        if (includeWarnings) {
            return warningsAndErrors;
        }
        return errors;
    }

    @Override
    public void warning(SAXParseException exception) {
        warningsAndErrors.add(exception);
    }

    @Override
    public void error(SAXParseException exception) {
        warningsAndErrors.add(exception);
        errors.add(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        warningsAndErrors.add(exception);
        errors.add(exception);
    }
}
