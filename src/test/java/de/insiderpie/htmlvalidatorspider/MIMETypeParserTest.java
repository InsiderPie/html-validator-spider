package de.insiderpie.htmlvalidatorspider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MIMETypeParserTest {

    @Test
    void getEssenceSimple() throws MIMETypeSyntaxException {
        String actual = MIMETypeParser.getEssence("text/html");
        assertEquals("text/html", actual);
    }

    @Test
    void getEssenceWithCharset() throws MIMETypeSyntaxException {
        String actual = MIMETypeParser.getEssence("text/html; charset=utf-8");
        assertEquals("text/html", actual);
    }

    @Test
    void getEssenceWithMultipleParameters() throws MIMETypeSyntaxException {
        String actual = MIMETypeParser.getEssence("text/html; charset=utf-8; foo=bar");
        assertEquals("text/html", actual);
    }

    @Test
    void throwIfEmpty() throws MIMETypeSyntaxException {
        assertThrows(MIMETypeSyntaxException.class, () -> MIMETypeParser.getEssence(""));
    }

    @Test
    void throwIfNoSubtype() throws MIMETypeSyntaxException {
        assertThrows(MIMETypeSyntaxException.class, () -> MIMETypeParser.getEssence("text"));
    }

    @Test
    void throwIfNoType() throws MIMETypeSyntaxException {
        assertThrows(MIMETypeSyntaxException.class, () -> MIMETypeParser.getEssence("/html"));
    }
}