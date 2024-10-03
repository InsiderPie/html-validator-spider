package de.insiderpie.htmlvalidatorspider;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import nu.validator.validation.SimpleDocumentValidator;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ValidatingCrawler {

    private static final String userAgent = "de.insiderpie.htmlvalidatorspider/1.0.0 java/" + System.getProperty("java.version");
    private final URI baseURI;
    private final CrawlFrontier htmlURIs;
    private final CrawlFrontier cssURIs;

    private boolean treatWarningsAsErrors = false;
    private boolean ignoreCSS = false;

    public URI getBaseURI() {
        return baseURI;
    }

    public boolean getTreatWarningsAsErrors() {
        return treatWarningsAsErrors;
    }

    public void setTreatWarningsAsErrors(boolean treatWarningsAsErrors) {
        this.treatWarningsAsErrors = treatWarningsAsErrors;
    }

    public boolean isIgnoreCSS() {
        return ignoreCSS;
    }

    public void setIgnoreCSS(boolean ignoreCSS) {
        this.ignoreCSS = ignoreCSS;
    }

    public ValidatingCrawler(URI baseURI) {
        this.baseURI = baseURI;
        htmlURIs = new CrawlFrontier();
        cssURIs = new CrawlFrontier();
        htmlURIs.enqueue(baseURI);
    }

    /**
     * Visit the baseURI, and all same-origin HTML pages and CSS files linked
     * from there, recursively, and return a list of validation results.
     */
    public List<ValidationResult> runValidation() {
        List<ValidationResult> validationResults = new ArrayList<>();

        while (!htmlURIs.isEmpty()) {
            URI uri = htmlURIs.next();
            String content = null;
            try {
                Optional<String> optionalContent = loadURIIfContentIsHTML(uri);
                if (optionalContent.isEmpty()) {
                    continue;
                }
                content = optionalContent.get();
                List<SAXParseException> parseErrors = validateHTML(uri, content);
                validationResults.add(ValidationResult.fromSAXParseErrors(uri, parseErrors));
            } catch (Exception e) {
                validationResults.add(ValidationResult.fromException(uri, e));
            }

            if (content != null) {
                parseHTMLAndDiscoverLinks(uri, content);
            }
        }

        while (!cssURIs.isEmpty()) {
            URI uri = cssURIs.next();
            String content = null;
            try {
                content = loadURIAsCSS(uri);
                List<SAXParseException> parseErrors = validateCSS(uri, content);
                validationResults.add(ValidationResult.fromSAXParseErrors(uri, parseErrors));
            } catch (Exception e) {
                validationResults.add(ValidationResult.fromException(uri, e));
            }

            if (content != null) {
                parseCSSAndDiscoverImports(uri, content);
            }
        }

        return validationResults;
    }

    private void parseHTMLAndDiscoverLinks(URI uri, String content) {
        // Follow the HTML 5 spec - ignore XML 1.0 errors
        XmlViolationPolicy policy = XmlViolationPolicy.ALLOW;
        InputSource inputSource = new InputSource(new StringReader(content));
        try {
            HtmlDocumentBuilder documentBuilder = new HtmlDocumentBuilder(policy);
            Document document = documentBuilder.parse(inputSource);
            document.setDocumentURI(uri.toString());
            discoverNewHTMLLinks(document);
            if (!ignoreCSS) {
                discoverNewCSSLinks(document);
            }
        } catch (Exception e) {
            System.err.printf("ERROR: could not discover links from %s: %s%n", uri, e.getMessage());
        }
    }

    private List<SAXParseException> validateHTML(URI uri, String content) throws Exception {
        InputSource inputSource = new InputSource(new StringReader(content));
        SimpleDocumentValidator validator = new SimpleDocumentValidator(true, false, false);
        CollectingErrorHandler errorHandler = new CollectingErrorHandler();
        validator.setUpMainSchema("http://s.validator.nu/html5-all.rnc", errorHandler);
        validator.setUpValidatorAndParsers(errorHandler, true, false);
        validator.checkHtmlInputSource(inputSource);
        return errorHandler.getErrors(treatWarningsAsErrors);
    }

    private List<SAXParseException> validateCSS(URI uri, String content) throws Exception {
        String html = wrapCSSInHTML(content);
        InputSource inputSource = new InputSource(new StringReader(html));
        SimpleDocumentValidator validator = new SimpleDocumentValidator(true, false, false);
        CollectingErrorHandler errorHandler = new CollectingErrorHandler();
        validator.setUpMainSchema("http://s.validator.nu/html5-all.rnc", errorHandler);
        validator.setUpValidatorAndParsers(errorHandler, true, false);
        validator.setAllowCss(true);
        validator.checkCssInputSource(inputSource);
        return errorHandler.getErrors(treatWarningsAsErrors);
    }

    private static String wrapCSSInHTML(String content) {
        return "<!DOCTYPE html><html lang=\"en\"><head><title>css</title><style>%s</style></head></html>".formatted(content);
    }

    private void discoverNewHTMLLinks(Document document) {
        NodeList anchors = document.getElementsByTagName("a");
        for (int i = 0; i < anchors.getLength(); i++) {
            Element anchor = (Element) anchors.item(i);
            String href = anchor.getAttribute("href");
            URI newURI = tryCreateURIWithoutHash(href, document.getDocumentURI());
            if (newURI == null || !sameOrigin(newURI, baseURI)) {
                continue;
            }
            htmlURIs.enqueue(newURI);
        }
    }

    private void discoverNewCSSLinks(Document document) {
        NodeList links = document.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            String rel = link.getAttribute("rel");
            if (!Objects.equals(rel, "stylesheet")) {
                continue;
            }
            String href = link.getAttribute("href");
            URI newURI = tryCreateURIWithoutHash(href, document.getDocumentURI());
            if (newURI == null || !sameOrigin(newURI, baseURI)) {
                continue;
            }
            cssURIs.enqueue(newURI);
        }
    }

    private void parseCSSAndDiscoverImports(URI uri, String content) {
        HashSet<String> imports = CSSImportParser.parse(content);
        for (String importURL : imports) {
            tryEnqueueCSSUrl(importURL, uri);
        }
    }

    private void tryEnqueueCSSUrl(String importURL, URI baseURI) {
        try {
            URI importURI = baseURI.resolve(new URI(importURL));
            cssURIs.enqueue(importURI);
        } catch (URISyntaxException e) {
            // Intentionally empty
        }
    }

    private static Optional<String> loadURIIfContentIsHTML(URI uri) throws NotAcceptableException, IOException, InterruptedException, MIMETypeSyntaxException {
        HttpResponse<String> response = loadURIAsString(uri, "text/html");
        if (!isContentType(response, "text/html")) {
            return Optional.empty();
        }
        return Optional.of(response.body());
    }

    private static String loadURIAsCSS(URI uri) throws NotAcceptableException, IOException, InterruptedException, MIMETypeSyntaxException {
        HttpResponse<String> response = loadURIAsString(uri, "text/css");
        assertContentTypeIs(response, "text/css");
        return response.body();
    }

    private static HttpResponse<String> loadURIAsString(URI uri, String accept) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(uri)
                .GET()
                .header("accept", accept)
                .header("user-agent", userAgent)
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static boolean isContentType(HttpResponse response, String expected) throws MIMETypeSyntaxException {
        Optional<String> contentType = response.headers().firstValue("content-type");
        if (contentType.isEmpty()) {
            return false;
        }
        String essence = MIMETypeParser.getEssence(contentType.get());
        return Objects.equals(essence, expected);
    }

    private static void assertContentTypeIs(HttpResponse response, String expected) throws MIMETypeSyntaxException, NotAcceptableException {
        Optional<String> contentType = response.headers().firstValue("content-type");
        if (contentType.isEmpty()) {
            throw new NotAcceptableException("(empty)", expected);
        } else {
            String essence = MIMETypeParser.getEssence(contentType.get());
            if (!Objects.equals(essence, expected)) {
                throw new NotAcceptableException(essence, expected);
            }
        }
    }

    private static URI tryCreateURIWithoutHash(String input, String base) {
        try {
            return createURIWithoutHash(input, base);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static URI createURIWithoutHash(String input, String base) throws URISyntaxException {
        URI inputURI = new URI(base).resolve(new URI(input));
        return new URIBuilder()
                .setScheme(inputURI.getScheme())
                .setUserInfo(inputURI.getUserInfo())
                .setHost(inputURI.getHost())
                .setPort(inputURI.getPort())
                .setPath(inputURI.getPath())
                .setCustomQuery(inputURI.getQuery())
                .build();
    }

    private static boolean sameOrigin(URI a, URI b) {
        return Objects.equals(a.getScheme(), b.getScheme())
                && Objects.equals(a.getHost(), b.getHost())
                && Objects.equals(a.getPort(), b.getPort());
    }
}
