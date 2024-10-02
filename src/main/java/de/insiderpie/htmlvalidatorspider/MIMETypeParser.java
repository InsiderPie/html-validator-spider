package de.insiderpie.htmlvalidatorspider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MIMETypeParser {
    // See the ABNF for a media-type in: https://datatracker.ietf.org/doc/html/rfc9110#name-collected-abnf
    final static String token = "[!#$%&'*+\\-.^_`|~A-Za-z0-9]";
    final static Pattern pattern = Pattern.compile("^%s+/%s+".formatted(token, token));

    /**
     * Parse the essence from a MIME Type string. For example,
     * <code>
     * parseEssence("text/html; charset=utf-8") -> "text/html"
     * </code>
     */
    public static String getEssence(String mimeType) throws MIMETypeSyntaxException {
        Matcher matcher = pattern.matcher(mimeType);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new MIMETypeSyntaxException(mimeType);
    }
}