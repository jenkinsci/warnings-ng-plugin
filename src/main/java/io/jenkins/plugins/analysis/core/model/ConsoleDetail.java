package io.jenkins.plugins.analysis.core.model;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import hudson.console.ConsoleNote;
import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Renders a section of the console log.
 *
 * @author Ullrich Hafner
 */
public class ConsoleDetail implements ModelObject {
    private int lineCount;

    /** The rendered source file. */
    private String sourceCode = StringUtils.EMPTY;

    private final Run<?, ?> owner;
    private final int from;
    private final int to;
    private final int end;
    private final int start;

    /**
     * Creates a new instance of this console log viewer object.
     *
     * @param owner
     *         the current build as owner of this view
     * @param consoleLog
     *         the lines of the console log
     * @param from
     *         first line in the console log
     * @param to
     *         last line in the console log
     */
    public ConsoleDetail(final Run<?, ?> owner, final Stream<String> consoleLog, final int from, final int to) {
        this.owner = owner;
        this.from = from;
        this.to = to;

        start = Math.max(1, from - 10);
        end = to + 10;

        readConsole(consoleLog.skip(start - 1).limit(end - start + 1));
    }

    private void readConsole(final Stream<String> consoleLog) {
        StringBuilder console = new StringBuilder(1024);

        console.append("<table>\n");
        lineCount = 0;
        consoleLog.forEach(line -> {
            console.append("<tr><td ");
            if (lineCount >= from - start && lineCount <= to - start) {
                console.append("style=\"background-color:#FCAF3E\"");
            }
            console.append(">");
            console.append(StringEscapeUtils.escapeHtml4(line));
            console.append("</td></tr>\n");
            lineCount++;
        });
        console.append("</table>\n");

        sourceCode = ConsoleNote.removeNotes(console.toString());
    }

    @Override
    public String getDisplayName() {
        return Messages.ConsoleLog_View_Title(start, end);
    }

    /**
     * Returns the build as owner of this view.
     *
     * @return the build
     */
    @SuppressWarnings("unused") // Called by jelly view to show the side panel
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the line that should be highlighted.
     *
     * @return the line to highlight
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getSourceCode() {
        return sourceCode;
    }
}
