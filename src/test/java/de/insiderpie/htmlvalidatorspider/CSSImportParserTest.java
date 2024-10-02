package de.insiderpie.htmlvalidatorspider;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CSSImportParserTest {

    @org.junit.jupiter.api.Test
    void parseSingleImport() throws IOException {
        String css = "@import url(\"https://example.com/index.css\");";
        HashSet<String> expected = new HashSet<>();
        expected.add("https://example.com/index.css");

        HashSet<String> actual = CSSImportParser.parse(css);

        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void parseTwoImports() throws IOException {
        String css = """
            @import url("https://example.com/base.css");
            @import url("https://example.com/index.css");
        """;
        HashSet<String> expected = new HashSet<>();
        expected.add("https://example.com/base.css");
        expected.add("https://example.com/index.css");

        HashSet<String> actual = CSSImportParser.parse(css);

        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void parseImportsWithLayer() throws IOException {
        String css = """
            @import url("https://example.com/base.css") layer(base);
            @import url("https://example.com/index.css");
        """;
        HashSet<String> expected = new HashSet<>();
        expected.add("https://example.com/base.css");
        expected.add("https://example.com/index.css");

        HashSet<String> actual = CSSImportParser.parse(css);

        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void parseImportsWithCondition() throws IOException {
        String css = """
            @import url("https://example.com/grid-layout.css") supports(display: grid);
            @import url("https://example.com/high-contrast.css") prefers-contrast(more);
            @import url("https://example.com/index.css");
        """;
        HashSet<String> expected = new HashSet<>();
        expected.add("https://example.com/grid-layout.css");
        expected.add("https://example.com/high-contrast.css");
        expected.add("https://example.com/index.css");

        HashSet<String> actual = CSSImportParser.parse(css);

        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void parseImportsAfterCharset() throws IOException {
        String css = """
            @charset(utf-8);
            @import url("https://example.com/index.css");
        """;
        HashSet<String> expected = new HashSet<>();
        expected.add("https://example.com/index.css");

        HashSet<String> actual = CSSImportParser.parse(css);

        assertEquals(expected, actual);
    }

    @org.junit.jupiter.api.Test
    void ignoreImportsAfterFirstCSSRule() throws IOException {
        String css = """
            @charset(utf-8);
            @import url("https://example.com/index.css");
            :root { }
            @import url("https://example.com/ignored.css");
        """;
        HashSet<String> expected = new HashSet<>();
        expected.add("https://example.com/index.css");

        HashSet<String> actual = CSSImportParser.parse(css);

        assertEquals(expected, actual);
    }
}