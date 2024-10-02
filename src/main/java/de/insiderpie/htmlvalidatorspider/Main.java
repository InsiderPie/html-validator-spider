package de.insiderpie.htmlvalidatorspider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        URI baseURI = getAndValidateBaseURI();
        boolean treatWarningsAsErrors = System.getenv("TREAT_WARNINGS_AS_ERRORS") != null;

        System.err.printf("Base URI: %s%nTreat warnings as errors: %b%n", baseURI, treatWarningsAsErrors);

        ValidatingCrawler crawler = new ValidatingCrawler(baseURI);
        crawler.setTreatWarningsAsErrors(treatWarningsAsErrors);
        List<ValidationResult> results = crawler.runValidation();
        printValidationResults(results);
        System.exit(countValidationErrors(results));
    }

    private static URI getAndValidateBaseURI() {
        String baseURL = System.getenv("BASE_URL");
        if (baseURL == null) {
            System.err.println("Environment variable BASE_URL must be set.");
            System.exit(-2);
        }
        URI baseURI = null;
        try {
            baseURI = new URI(baseURL);
        } catch (URISyntaxException e) {
            System.err.println("Environment variable BASE_URL must be a valid URL that can be parsed by new URI(), but was: " + baseURL);
            System.exit(-2);
        }
        if (!baseURI.isAbsolute()) {
            System.err.println("Environment variable BASE_URL must be an absolute URL, but was: " + baseURL);
            System.exit(-2);
        }
        if (!Objects.equals(baseURI.getScheme(), "http") && !Objects.equals(baseURI.getScheme(), "https")) {
            System.err.println("Environment variable BASE_URL must be an HTTP or HTTPS URL, but was: " + baseURL);
            System.exit(-2);
        }
        return baseURI;
    }

    private static void printValidationResults(List<ValidationResult> results) {
        for (ValidationResult result : results) {
            if (result.isOk()) {
                System.out.printf("OK:  %s%n", result.getUri());
            } else {
                System.out.printf("ERR: %s%n%s%n", result.getUri(), result.getMessage());
            }
        }
    }

    private static int countValidationErrors(List<ValidationResult> results) {
        return (int) results.stream().filter(result -> !result.isOk()).count();
    }
}
