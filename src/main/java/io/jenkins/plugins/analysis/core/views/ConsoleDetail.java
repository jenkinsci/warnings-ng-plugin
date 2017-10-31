package io.jenkins.plugins.analysis.core.views;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.Issue;

import hudson.console.ConsoleNote;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;

/**
 * Renders a section of the console log.
 *
 * @author Ulli Hafner
 */
public class ConsoleDetail implements ModelObject {
    /** Filename dummy if the console log is the source of the warning. */
    public static final String CONSOLE_LOG_FILENAME = "Console Log";

    public static boolean isInConsoleLog(final Issue issue) {
        return issue.getFileName().equals(ConsoleDetail.CONSOLE_LOG_FILENAME);
    }

    /** The current build as owner of this object. */
    private final Run<?, ?> owner;
    /** The rendered source file. */
    private String sourceCode = StringUtils.EMPTY;
    private final int from;
    private final int to;
    private final int end;
    private final int start;

    /**
     * Creates a new instance of this console log viewer object.
     *
     * @param owner
     *         the current build as owner of this object
     * @param from
     *         first line in the console log
     * @param to
     *         last line in the console log
     */
    public ConsoleDetail(final Run<?, ?> owner, final int from, final int to) {
        this.owner = owner;
        this.from = from;
        this.to = to;

        start = Math.max(0, from - 10);
        end = to + 10;

        readConsole();
    }

    private void readConsole() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(owner.getLogFile()), "UTF8"));
            StringBuilder console = new StringBuilder(1024);

            console.append("<table>\n");
            int lineCount = 0;
            for (String line = reader.readLine(); line != null && lineCount <= end; line = reader.readLine()) {
                if (lineCount >= start) {
                    console.append("<tr><td ");
                    if (lineCount >= from && lineCount <= to) {
                        console.append("style=\"background-color:#FCAF3E\"");
                    }
                    console.append(">\n");
                    console.append(StringEscapeUtils.escapeHtml(line));
                    console.append("</td></tr>\n");
                }
                lineCount++;
            }
            console.append("</table>\n");

            sourceCode = ConsoleNote.removeNotes(console.toString());
        }
        catch (IOException exception) {
            sourceCode = sourceCode + exception.getLocalizedMessage();
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public String getDisplayName() {
        return Messages.ConsoleLog_Title(start, end);
    }

    /**
     * Gets the file name of this source file.
     *
     * @return the file name
     */
    public String getFileName() {
        return getDisplayName();
    }

    /**
     * Returns the build as owner of this object.
     *
     * @return the build
     */
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the line that should be highlighted.
     *
     * @return the line to highlight
     */
    public String getSourceCode() {
        return sourceCode;
    }
}
