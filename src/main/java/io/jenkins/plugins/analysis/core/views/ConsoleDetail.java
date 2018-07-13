package io.jenkins.plugins.analysis.core.views;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;

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
    /**
     * Returns whether the specified issue refers to a line in the console log.
     *
     * @param issue
     *         the issue to check
     *
     * @return {@code true} if the issue refers to a line in the console log, {@code false} if the issue refers to a
     *         source code file in the workspace
     */
    public static boolean isInConsoleLog(final Issue issue) {
        return IssueParser.isSelfReference(issue);
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
        try (BufferedReader reader = openConsoleLog()) {
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
                    console.append(StringEscapeUtils.escapeHtml4(line));
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
    }

    private BufferedReader openConsoleLog() throws UnsupportedEncodingException, FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(owner.getLogFile()), "UTF8"));
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
