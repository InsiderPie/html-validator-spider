package de.insiderpie.htmlvalidatorspider;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSSImportParser {
    private final static Pattern commentPattern = Pattern.compile("/\\*.*\\*/");
    private final static Pattern importPattern = Pattern.compile("@import\s+?url\\(\"(?<url>.+?)\"\\).*?;");
    private final static Pattern unescapedOpenBracketPattern = Pattern.compile("(?<!\\\\)\\{");

    /**
     * Parse the given CSS and return a set of identified import URLs.
     * All specified import URLs are returned regardless of any conditions
     * (supports or media queries). Imports after the first CSS statement
     * are ignored as per the CSS specification.
     */
    public static HashSet<String> parse(String css) {
        HashSet<String> urls = new HashSet<>();
        String[] uncommentedParts = commentPattern.split(css);
        for (String part : uncommentedParts) {
            int endIndex = indexOfFirstUnescapedOpenBracket(part);
            if (endIndex != -1) {
                part = part.substring(0, endIndex);
            }
            Matcher matcher = importPattern.matcher(part);
            while (matcher.find()) {
                urls.add(matcher.group("url"));
            }
            if (endIndex != -1) {
                break;
            }
        }
        return urls;
    }

    private static int indexOfFirstUnescapedOpenBracket(String css) {
        Matcher matcher = unescapedOpenBracketPattern.matcher(css);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }
}
