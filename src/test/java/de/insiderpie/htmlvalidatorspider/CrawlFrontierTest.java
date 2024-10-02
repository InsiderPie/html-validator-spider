package de.insiderpie.htmlvalidatorspider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
class CrawlFrontierTest {
    CrawlFrontier crawlFrontier;

    @BeforeEach
    void setUp() {
        crawlFrontier = new CrawlFrontier();
    }

    @Test
    void isInitiallyEmpty() {
        assertTrue(crawlFrontier.isEmpty());
    }

    @Test
    void addingURIReturnsTrue() throws URISyntaxException {
        assertTrue(crawlFrontier.enqueue(new URI("https://example.com")));
    }

    @Test
    void isNotEmptyAfterAddingURI() throws URISyntaxException {
        crawlFrontier.enqueue(new URI("https://example.com"));
        assertFalse(crawlFrontier.isEmpty());
    }

    @Test
    void nextReturnsAddedURI() throws URISyntaxException {
        crawlFrontier.enqueue(new URI("https://example.com"));
        assertEquals(new URI("https://example.com"), crawlFrontier.next());
    }

    @Test
    void isEmptyAfterMarkingAddedURIAsDone() throws URISyntaxException {
        crawlFrontier.enqueue(new URI("https://example.com"));
        crawlFrontier.next();
        assertTrue(crawlFrontier.isEmpty());
    }

    @Test
    void reAddingAlreadyDoneURIReturnsFalse() throws URISyntaxException {
        crawlFrontier.enqueue(new URI("https://example.com"));
        crawlFrontier.next();
        assertFalse(crawlFrontier.enqueue(new URI("https://example.com")));
    }

    @Test
    void isEmptyAfterReAddingAlreadyDoneURI() throws URISyntaxException {
        crawlFrontier.enqueue(new URI("https://example.com"));
        crawlFrontier.next();
        crawlFrontier.enqueue(new URI("https://example.com"));
        assertTrue(crawlFrontier.isEmpty());
    }

    @Test
    void isNotEmptyAfterAddingMultipleURIs() throws URISyntaxException {
        crawlFrontier.enqueue(new URI("https://example.com"));
        crawlFrontier.enqueue(new URI("https://example.net"));
        assertFalse(crawlFrontier.isEmpty());
    }

    @Test
    void nextReturnsAddedURIs() throws URISyntaxException {
        crawlFrontier.enqueue(new URI("https://example.com"));
        crawlFrontier.enqueue(new URI("https://example.net"));

        HashSet<URI> expected = new HashSet<>();
        expected.add(new URI("https://example.com"));
        expected.add(new URI("https://example.net"));

        HashSet<URI> actual = new HashSet<>();
        actual.add(crawlFrontier.next());
        actual.add(crawlFrontier.next());

        assertEquals(expected, actual);
    }
}