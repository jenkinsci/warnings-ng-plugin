package io.jenkins.plugins.analysis.core.model;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.util.LookaheadStream;

import j2html.tags.ContainerTag;
import j2html.tags.UnescapedText;

import io.jenkins.plugins.analysis.core.util.Sanitizer;

import static j2html.TagCreator.*;

/**
 * Renders a source code file into a HTML snippet using Prism.js.
 *
 * @author Philippe Arteau
 * @author Ullrich Hafner
 */
public class SourcePrinter {
    private static final Sanitizer SANITIZER = new Sanitizer();

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
        LookaheadStream stream = new LookaheadStream(lines);

        int start = issue.getLineStart();
        int end = issue.getLineEnd();

        StringBuilder before = readBlockUntilLine(stream, start - 1);
        StringBuilder marked = readBlockUntilLine(stream, end);
        StringBuilder after = readBlockUntilLine(stream, Integer.MAX_VALUE);

        String language = selectLanguageClass(issue);
        String code = asCode(before, language, "line-numbers")
                + asCode(marked, language, "highlight")
                + createInfoPanel(issue, description, start, iconUrl)
                + asCode(after, language);

        return pre().with(new UnescapedText(code)).renderFormatted();
    }

    private StringBuilder readBlockUntilLine(final LookaheadStream stream, final int end) {
        StringBuilder marked = new StringBuilder();
        while (stream.hasNext() && stream.getLine() < end) {
            marked.append(stream.next());
            marked.append("\n");
        }
        return marked;
    }

    private String createInfoPanel(final Issue issue, final String description, final int start,
            final String iconUrl) {
        if (StringUtils.isEmpty(description)) {
            return createMessage(issue.getMessage(), iconUrl).render();

        }
        return createDescription(issue.getMessage(), description, start, iconUrl).render();
    }

    private ContainerTag createMessage(final String message, final String iconUrl) {
        return div().withClass("analysis-warning").with(messageLabelFrom(message, iconUrl));
    }

    private ContainerTag messageLabelFrom(final String message, final String iconUrl) {
        return label().withClass("collapse-btn").with(table().with(
                tr().with(td().with(img().withSrc(iconUrl),
                        td().withClass("analysis-title-column").with(div().withClass("analysis-warning-title").with(unescape(message)))))));
    }

    private ContainerTag createDescription(final String message, final String description, final int line,
            final String iconUrl) {
        String id = "collapse-" + line;
        return div().withClass("analysis-warning").with(
                input().withClass("collapse-open").withId(id).attr("type", "checkbox"),
                messageLabelFrom(message, iconUrl).attr("for", id),
                div().withClass("collapse-panel").with(
                        div().withClasses("collapse-inner", "analysis-detail").with(unescape(description))));
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

    private String asCode(final StringBuilder text, final String... classes) {
        return code().withClasses(classes).with(unescape(StringEscapeUtils.escapeHtml4(text.toString()))).render();
    }
}
