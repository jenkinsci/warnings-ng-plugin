package hudson.plugins.analysis.util;

/**
 * Simple wrapper of a {@link StringBuffer} that is capable of writing HTML sequences used in Jelly scripts.
 *
 * @author Ulli Hafner
 */
public class HtmlPrinter {
    private final StringBuilder buffer = new StringBuilder();

    /**
     * Adds a list item.
     *
     * @param text
     *            the item text
     * @return HTML item
     */
    public String item(final String text) {
        return String.format("<li>%s</li>", text);
    }

    /**
     * Adds a table line. A line is a row with one column.
     *
     * @param text
     *            the line text
     * @return table line
     */
    public String line(final String text) {
        return String.format("<tr><td>%s</td></tr>", text);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    /**
     * Adds a hyperlink.
     *
     * @param url
     *            the link URL
     * @param text
     *            the text describing the link
     * @return HTML link
     */
    public String link(final String url, final String text) {
        return String.format("<a href=\"%s\">%s</a>", url, text);
    }

    /**
     * Appends the specified text to the buffer.
     *
     * @param text
     *            the text
     */
    public void append(final String text) {
        buffer.append(text);
    }

    /**
     * Appends the textual representation of the specified object to the buffer.
     *
     * @param object
     *            the object
     */
    public void append(final Object object) {
        buffer.append(object);
    }
}
