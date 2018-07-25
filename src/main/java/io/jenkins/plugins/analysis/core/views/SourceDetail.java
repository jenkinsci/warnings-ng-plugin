package io.jenkins.plugins.analysis.core.views;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;
import edu.hm.hafner.analysis.Issue;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Renders a source file containing an issue for the whole file or a specific line number.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class SourceDetail implements ModelObject {
    /** Offset of the source code generator. After this line the actual source file lines start. */
    private static final int SOURCE_GENERATOR_OFFSET = 13;
    /** Color for the first (primary) issue range. */
    private static final String FIRST_COLOR = "#FCAF3E";
    /** Color for all other issue ranges. */
    private static final String OTHER_COLOR = "#FCE94F";
    /** The current build as owner of this object. */
    private final Run<?, ?> owner;
    /** The issue to be shown. */
    private final Issue issue;
    /** The rendered source file. */
    private final String sourceCode;

    /**
     * Creates a new instance of this source code object.
     *
     * @param owner
     *         the current build as owner of this view
     * @param affectedFile
     *         the file to show
     * @param issue
     *         the warning to display in the source file
     */
    public SourceDetail(final Run<?, ?> owner, final Reader affectedFile, final Issue issue) {
        this.owner = owner;
        this.issue = issue;

        sourceCode = renderSourceCode(affectedFile);
    }

    private String renderSourceCode(final Reader affectedFile) {
        try {
            return splitSourceFile(highlightSource(affectedFile));
        }
        catch (IOException e) {
            return e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e);
        }
    }

    @Override
    public String getDisplayName() {
        return issue.getBaseName();
    }

    /**
     * Highlights the specified source and returns the result as an HTML string.
     *
     * @param file
     *         the source file to highlight
     *
     * @return the source as an HTML string
     * @throws IOException
     *         if the source code could not be read
     */
    private String highlightSource(final Reader file) throws IOException {
        JavaSource source = new JavaSourceParser().parse(file);

        JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
        StringWriter writer = new StringWriter();
        JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
        options.setShowLineNumbers(true);
        options.setAddLineAnchors(true);
        converter.convert(source, options, writer);

        return writer.toString();
    }

    /**
     * Splits the source code into three blocks: the line to highlight and the source code before and after this line.
     *
     * @param sourceFile
     *         the source code of the whole file as rendered HTML string
     */
    // CHECKSTYLE:CONSTANTS-OFF
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    private String splitSourceFile(final String sourceFile) {
        StringBuilder output = new StringBuilder(sourceFile.length());

        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(sourceFile));

        // TODO: add support for line ranges (or replace this view with client side rendering)
        try {
            int lineNumber = 1;
            while (lineNumber < SOURCE_GENERATOR_OFFSET) {
                copyLine(output, lineIterator);
                lineNumber++;
            }
            lineNumber = 1;
            boolean isFirstRange = true;
            while (lineNumber < issue.getLineStart()) {
                copyLine(output, lineIterator);
                lineNumber++;
            }
            output.append("</code>\n");
            output.append("</td></tr>\n");
            output.append("<tr><td style=\"background-color:");
            appendRangeColor(output, isFirstRange);
            output.append("\">\n");
            output.append("<div id=\"line");
            output.append(issue.getLineStart());
            output.append("\" tooltip=\"");
            if (issue.getLineStart() > 0) {
                outputEscaped(output, issue.getMessage());
            }
            outputEscaped(output, issue.getDescription());
            output.append("\" nodismiss=\"\">\n");
            output.append("<code><b>\n");
            if (issue.getLineStart() <= 0) {
                output.append(issue.getMessage());
                if (StringUtils.isBlank(issue.getMessage())) {
                    output.append(issue.getDescription());
                }
            }
            else {
                while (lineNumber <= issue.getLineEnd()) {
                    copyLine(output, lineIterator);
                    lineNumber++;
                }
            }
            output.append("</b></code>\n");
            output.append("</div>\n");
            output.append("</td></tr>\n");
            output.append("<tr><td>\n");
            output.append("<code>\n");
            isFirstRange = false;
            while (lineIterator.hasNext()) {
                copyLine(output, lineIterator);
            }
        }
        catch (NoSuchElementException exception) {
            // ignore an illegal range
        }
        return output.toString();
    }
    // CHECKSTYLE:CONSTANTS-ON

    /**
     * Writes the message to the output stream (with escaped HTML).
     *
     * @param output
     *         the output to write to
     * @param message
     *         the message to write
     */
    private void outputEscaped(final StringBuilder output, final String message) {
        output.append(StringEscapeUtils.escapeHtml4(message));
    }

    /**
     * Appends the right range color.
     *
     * @param output
     *         the output to append the color
     * @param isFirstRange
     *         determines whether the range is the first one
     */
    private void appendRangeColor(final StringBuilder output, final boolean isFirstRange) {
        if (isFirstRange) {
            output.append(FIRST_COLOR);
        }
        else {
            output.append(OTHER_COLOR);
        }
    }

    /**
     * Copies the next line of the input to the output.
     *
     * @param output
     *         output
     * @param lineIterator
     *         input
     */
    private void copyLine(final StringBuilder output, final LineIterator lineIterator) {
        output.append(lineIterator.nextLine());
        output.append('\n');
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
    public String getSourceCode() {
        return sourceCode;
    }
}

