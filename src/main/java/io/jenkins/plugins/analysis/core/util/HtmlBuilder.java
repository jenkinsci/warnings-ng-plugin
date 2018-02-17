package io.jenkins.plugins.analysis.core.util;

/**
 * Simple wrapper of a {@link StringBuilder} that is capable of writing HTML sequences used in Jelly scripts.
 *
 * @author Ulli Hafner
 */
// FIXME: replace with lib
public class HtmlBuilder {
    private final StringBuilder buffer = new StringBuilder();

    @Override
    public String toString() {
        return buffer.toString();
    }

    /**
     * Adds a hyperlink (href).
     *
     * @param url
     *         the link URL
     * @param text
     *         the text describing the link
     *
     * @return HTML link
     */
    public HtmlBuilder link(final String url, final String text) {
        buffer.append(String.format("<a href=\"%s\">%s</a>", url, text));

        return this;
    }

    /**
     * Adds a hyperlink (href).
     *
     * @param url
     *         the link URL
     * @param clazz
     *         the class of the href
     * @param text
     *         the text describing the link
     *
     * @return HTML link
     */
    public HtmlBuilder linkWithClass(final String url, final String text, final String clazz) {
        buffer.append(String.format("<a href=\"%s\" class=\"%s\">%s</a>", url, clazz, text));

        return this;
    }

    /**
     * Creates the HTML snippet.
     *
     * @return the HTML snippet produced so far
     */
    public String build() {
        return buffer.toString();
    }
}
