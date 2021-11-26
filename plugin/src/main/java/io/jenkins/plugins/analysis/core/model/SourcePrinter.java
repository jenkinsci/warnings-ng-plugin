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
 */
@SuppressWarnings("PMD.GodClass")
public class SourcePrinter {
    private static final Sanitizer SANITIZER = new Sanitizer();
    private final JenkinsFacade jenkinsFacade;
    private final ColumnMarker columnMarker = new ColumnMarker("ACOMBINATIONthatIsUnliklyTobe_in_any_source_code");

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
            String code = asCode(before, language, "line-numbers")
                    + asMarkedCode(marked, issue,  language, "highlight")
                    + createInfoPanel(issue, description, iconUrl)
                    + asCode(after, language);

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
                td().with(img().withSrc(iconUrl)),
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
        final StringBuilder marked = columnMarker.markColumns(text.toString(), issue.getColumnStart(), issue.getColumnEnd());
        final String sanitized = SANITIZER.render(StringEscapeUtils.escapeHtml4(marked.toString()));
        final String markerReplaced = columnMarker.replaceMarkerWithHtmlTag(sanitized);
        final UnescapedText unescapedText = new UnescapedText(markerReplaced);
        return code().withClasses(classes).with(unescapedText).render();
    }

    private String asCode(final StringBuilder text, final String... classes) {
        return code().withClasses(classes).with(unescape(StringEscapeUtils.escapeHtml4(text.toString()))).render();
    }
}
