package io.jenkins.plugins.analysis.core.model;

/**
 * Encloses columns between start and end with the HTML tag 'mark'.
 *
 */
public final class ColumnMarker {
    /**
     * Creates ColumnMarker will use `placeHolderText` for enclosing.
     *
     * @param placeHolderText
     *         Used to construct an opening and closing text that can
     *         later be replaced with the HTML tag.
     *         It should be a text that is unlikely to appear in any source code.
     */
    public ColumnMarker(final String placeHolderText) {
        openingTagPlaceHolder = "OpEn" + placeHolderText;
        closingTagPlaceHolder = "ClOsE" + placeHolderText;
    }
    private final String openingTagPlaceHolder;
    private final String closingTagPlaceHolder;
    private final String openingTag = "<span class='squiggled-underline'>";
    private final String closingTag = "</span>";
    /**
     * Encloses columns between start and end with the HTML tag 'mark'.
     * This will make prism highlight the enclosed part of the line.
     *
     * @param text
     *         the source code line
     * @param start
     *         the first column in text, that needs to be marked
     * @param end
     *         the last column in text, that needs to be marked
     *
     * @return StringBuilder containing the text with the added html tag "mark"
     */
    public StringBuilder markColumns(final String text, final int start, final int end) {
        if (start < 1 || text.length() == 0 || end > text.length()) {
            return new StringBuilder(text);
        }
        final int realStart = start - 1;
        final int realEnd = (end == 0) ? text.length() - 1 : end - 1;

        if (realStart > realEnd) {
            return new StringBuilder(text);
        }
        final int afterMark = realEnd + 1;

        final String before = text.substring(0, realStart);
        final String toBeMarked = text.substring(realStart, afterMark);
        final String after = text.substring(afterMark);

        return new StringBuilder(before)
                .append(openingTagPlaceHolder)
                .append(toBeMarked)
                .append(closingTagPlaceHolder)
                .append(after);
    }
    /**
     * Encloses columns between start and end with the HTML tag 'mark'.
     * This will make prism highlight the enclosed part of the line.
     *
     * @param text
     *         the source code line
     *
     * @return String containing the text with the added html tag
     */
    public String replacePlaceHolderWithHtmlTag(final String text) {
        return text.replaceAll(openingTagPlaceHolder, openingTag)
                   .replaceAll(closingTagPlaceHolder, closingTag);
    }
}
