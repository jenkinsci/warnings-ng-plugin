package io.jenkins.plugins.analysis.core.model;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.util.LookaheadStream;
import edu.hm.hafner.util.VisibleForTesting;

import j2html.tags.ContainerTag;
import j2html.tags.UnescapedText;

import io.jenkins.plugins.analysis.core.util.Sanitizer;
import io.jenkins.plugins.fontawesome.api.SvgTag;
import io.jenkins.plugins.util.JenkinsFacade;

import static j2html.TagCreator.*;

/**
 * Renders a source code file into a HTML snippet using Prism.js.
 *
 * @author Philippe Arteau
 * @author Ullrich Hafner
 * @deprecated moved to prism-api-plugin
 */
@SuppressWarnings("PMD.GodClass")
@Deprecated
public class SourcePrinter {
    private static final Sanitizer SANITIZER = new Sanitizer();

    private static final ColumnMarker COLUMN_MARKER = new ColumnMarker("ACOMBINATIONthatIsUnliklyTobe_in_any_source_code");
    private static final String LINE_NUMBERS = "line-numbers";
    private static final String MATCH_BRACES = "match-braces";

    private final JenkinsFacade jenkinsFacade;

    /**
     * Creates a new instance of {@link SourcePrinter}.
     */
    public SourcePrinter() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    SourcePrinter(final JenkinsFacade jenkinsFacade) {
        this.jenkinsFacade = jenkinsFacade;
    }

    /**
     * Creates a colorized HTML snippet with the specified source code. Highlights the specified issue and provides a
     * clickable and collapsible element that shows the details for the issue.
     *
     * @param lines
     *         the lines of the source code
     * @param issue
     *         the issue to show
     * @param description
     *         an additional description for the issue
     * @param iconUrl
     *         absolute URL to the small icon of the static analysis tool
     *
     * @return the source code as colorized HTML
     */
    public String render(final Stream<String> lines, final Issue issue, final String description,
            final String iconUrl) {
        try (LookaheadStream stream = new LookaheadStream(lines)) {
            int start = issue.getLineStart();
            int end = issue.getLineEnd();

            StringBuilder before = readBlockUntilLine(stream, start - 1);
            StringBuilder marked = readBlockUntilLine(stream, end);
            StringBuilder after = readBlockUntilLine(stream, Integer.MAX_VALUE);

            String language = selectLanguageClass(issue);
            String code = asCode(before, language, LINE_NUMBERS, MATCH_BRACES)
                    + asMarkedCode(marked, issue, language, LINE_NUMBERS, "highlight", MATCH_BRACES)
                    + createInfoPanel(issue, description, iconUrl)
                    + asCode(after, language, LINE_NUMBERS, MATCH_BRACES);

            return pre().with(new UnescapedText(code)).renderFormatted();
        }
    }

    private StringBuilder readBlockUntilLine(final LookaheadStream stream, final int end) {
        StringBuilder marked = new StringBuilder();
        while (stream.hasNext() && stream.getLine() < end) {
            marked.append(stream.next());
            marked.append("\n");
        }
        return marked;
    }

    private String createInfoPanel(final Issue issue, final String description,
            final String iconUrl) {
        return createIssueBox(issue, description, iconUrl).withClass("analysis-warning").render();
    }

    private ContainerTag createIssueBox(final Issue issue, final String description,
            final String iconUrl) {
        if (StringUtils.isEmpty(description)) {
            return createTitle(issue.getMessage(), iconUrl, false);
        }
        else {
            return createTitleAndCollapsedDescription(issue.getMessage(), description, iconUrl);
        }
    }

    private ContainerTag createTitle(final String message, final String iconUrl, final boolean isCollapseVisible) {
        return div().with(table().withClass("analysis-title").with(tr().with(
                td().with(img().withClass("analysis-icon").withSrc(iconUrl)),
                td().withClass("analysis-title-column")
                        .with(div().withClass("analysis-warning-title").with(replaceNewLine(message))),
                createCollapseButton(isCollapseVisible)
        )));
    }

    private ContainerTag createCollapseButton(final boolean isCollapseVisible) {
        ContainerTag td = td();
        if (isCollapseVisible) {
            td.with(new UnescapedText(new SvgTag("chevron-circle-down", jenkinsFacade)
                    .withClasses("analysis-collapse-icon").render()));
        }
        return td;
    }

    private ContainerTag createTitleAndCollapsedDescription(final String message, final String description,
            final String iconUrl) {
        return div().with(
                div().withClass("analysis-collapse-button").with(createTitle(message, iconUrl, true)),
                div().withClasses("collapse", "analysis-detail")
                        .with(unescape(description))
                        .withId("analysis-description"));
    }

    private UnescapedText replaceNewLine(final String message) {
        String m = message.replace("\n", "<br>");
        return unescape(m);
    }

    private UnescapedText unescape(final String message) {
        return new UnescapedText(SANITIZER.render(message));
    }

    @SuppressWarnings({"javancss", "PMD.CyclomaticComplexity"})
    private String selectLanguageClass(final Issue issue) {
        switch (StringUtils.defaultIfEmpty(StringUtils.substringAfterLast(issue.getBaseName(), "."),
                issue.getBaseName())) {
            case "htm":
            case "html":
            case "xml":
            case "xsd":
                return "language-markup";
            case "css":
                return "language-css";
            case "js":
                return "language-javascript";
            case "c":
                return "language-c";
            case "cs":
                return "language-csharp";
            case "cpp":
                return "language-cpp";
            case "Dockerfile":
                return "language-docker";
            case "go":
                return "language-go";
            case "groovy":
                return "language-groovy";
            case "json":
                return "language-json";
            case "md":
                return "language-markdown";
            case "erb":
            case "jsp":
            case "tag":
                return "language-erb";
            case "jav":
            case "java":
                return "language-java";
            case "rb":
                return "language-ruby";
            case "kt":
                return "language-kotlin";
            case "vb":
                return "language-vbnet";
            case "pl":
                return "language-perl";
            case "php":
                return "language-php";
            case "py":
                return "language-python";
            case "sql":
                return "language-sql";
            case "scala":
            case "sc":
                return "language-scala";
            case "swift":
                return "language-swift";
            case "ts":
                return "language-typescript";
            case "yaml":
                return "language-yaml";
            default:
                return "language-clike"; // Best effort for unknown extensions
        }
    }

    private String asMarkedCode(final StringBuilder text, final Issue issue, final String... classes) {
        final StringBuilder marked = (issue.getLineStart() == issue.getLineEnd())
                ? COLUMN_MARKER.markColumns(text.toString(), issue.getColumnStart(), issue.getColumnEnd())
                : text;
        final String sanitized = SANITIZER.render(StringEscapeUtils.escapeHtml4(marked.toString()));
        final String markerReplaced = COLUMN_MARKER.replacePlaceHolderWithHtmlTag(sanitized);
        final UnescapedText unescapedText = new UnescapedText(markerReplaced);
        return code().withClasses(classes).with(unescapedText).render();
    }

    private String asCode(final StringBuilder text, final String... classes) {
        return code().withClasses(classes).with(unescape(StringEscapeUtils.escapeHtml4(text.toString()))).render();
    }

    /**
     * Encloses columns between {@code start} and {@code end} with an HTML tag (see {@code openingTag} and
     * {@code closingTag}).
     */
    static final class ColumnMarker {
        private static final String OPENING_TAG = "<span class='code-mark'>";
        private static final String CLOSING_TAG = "</span>";

        /**
         * Creates a {@link ColumnMarker} that will use {@code placeHolderText} for enclosing.
         *
         * @param placeHolderText
         *         Used to construct an opening and closing text that can later be replaced with the HTML tag {@code
         *         openingTag} {@code closingTag}. It should be a text that is unlikely to appear in any source code.
         */
        ColumnMarker(final String placeHolderText) {
            openingTagPlaceHolder = "OpEn" + placeHolderText;
            closingTagPlaceHolder = "ClOsE" + placeHolderText;
        }

        private final String openingTagPlaceHolder;
        private final String closingTagPlaceHolder;

        /**
         * Encloses columns between start and end with the HTML tag {@code openingTag} {@code closingTag}. This will
         * make prism highlight the enclosed part of the line.
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
         * Encloses columns between start and end with the HTML tag {@code openingTag} {@code closingTag}. This will
         * make prism highlight the enclosed part of the line.
         *
         * @param text
         *         the source code line
         *
         * @return String containing the text with the added html tag
         */
        public String replacePlaceHolderWithHtmlTag(final String text) {
            return text.replaceAll(openingTagPlaceHolder, OPENING_TAG)
                    .replaceAll(closingTagPlaceHolder, CLOSING_TAG);
        }
    }
}
