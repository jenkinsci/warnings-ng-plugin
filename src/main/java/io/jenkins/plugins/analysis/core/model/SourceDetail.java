package io.jenkins.plugins.analysis.core.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.LineRange;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Renders a source file containing an issue for the whole file or a specific line number.
 *
 * @author Ullrich Hafner
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
    /** a detailed description of the specified issue. */
    private final String description;

    /**
     * Creates a new instance of this source code object.
     *
     * @param owner
     *         the current build as owner of this view
     * @param affectedFile
     *         the file to show
     * @param issue
     *         the issue to show in the source file
     * @param description
     *         a detailed description of the specified issue
     */
    public SourceDetail(final Run<?, ?> owner, final Reader affectedFile, final Issue issue,
            final String description) {
        this.owner = owner;
        this.issue = issue;
        this.description = description;

        sourceCode = renderSourceCode(affectedFile);
    }

    private String renderSourceCode(final Reader affectedFile) {
        try {
            return buildCodeBlock(affectedFile,issue.getFileName());
        }
        catch (IOException e) {
            return e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e);
        }
    }


    /**
     * Prism does not detect the language by itself.
     * Base on the extension of the file, the language is detected.
     *
     * @param extension File extension without the dot.
     * @return
     */
    private String prismLangClassFromExtension(String extension) {

        switch(extension.toLowerCase()) {
            case "jav": case "java":
                return "language-java";
            case "htm": case "html": case "xml": case "xsd":
                return "language-markup";
            case "erb": case "jsp": case "tag":
                return "language-erb";
            case "rb":
                return "language-ruby";
            case "kt":
                return "language-kotlin";
            case "js":
                return "language-javascript";
            case "c":
                return "language-c";
            case "cs":
                return "language-csharp";
            case "vb":
                return "language-vbnet";
            case "cpp":
                return "language-cpp";
            case "groovy":
                return "language-groovy";
            case "pl":
                return "language-perl";
            case "php":
                return "language-php";
            case "py":
                return "language-python";
            case "scala": case "sc":
                return "language-scala";
        }

        return "language-clike"; //Best effort for unknown extensions
    }

    /**
     * This function build
     *
     * @param sourceFileReader Source file containing the source code to be displayed.
     * @param fileName The filename associated to the content. It will be use to select the syntax highlight.
     * @return
     * @throws IOException
     */
    private String buildCodeBlock(Reader sourceFileReader, String fileName) throws IOException {

        StringWriter writer = new StringWriter();

        String prismCssClass = prismLangClassFromExtension(FilenameUtils.getExtension(fileName));
        writer.append("<pre><code class=\""+prismCssClass+" line-numbers\">");

        BufferedReader reader = new BufferedReader(sourceFileReader);

        List<LineRange> ranges = new ArrayList<LineRange>(); //issue.getLineRanges(); //There are only one line highlight at the time for the moment.
        if(ranges.size() == 0) {
            ranges.add(new LineRange(issue.getLineStart(), issue.getLineEnd()));
        }
        LineRange activeRange = null;
        boolean activeRegularBlock = false;

        int lineNumber = 0;
        String line = null;
        while((line = reader.readLine()) != null) {
            lineNumber++;

            /** Start highlight block **/
            if(activeRange == null) {
                boolean newBlock = false;
                ranges: for(LineRange r :ranges) {
                    if(r.getStart() == lineNumber) {
                        newBlock = true;
                        activeRange = r;
                        break ranges;
                    }
                }

                if(newBlock) {
                    writer.append("</code><code class=\"highlight\" id=\"focus\">");
                }

            }

            /** Code **/
            writer.append(line);
            writer.append("\n");

            /** Code highlight block with bug description **/
            if(activeRange != null) {
                if (lineNumber == activeRange.getEnd()) { //Closing active block

                    ranges.remove(activeRange);
                    activeRange = null;

                    writer.append("</code>");

                    if(!description.equals("")) { //Issue that has a detailed description

                        int id = lineNumber; //Line number used as unique ID
                        writer.append("<div class=\"analysis-warning\">\n" +
                                "    <input class=\"collapse-open\" type=\"checkbox\" id=\"collapse-"+id+"\"/>\n" +
                                "    <label class=\"collapse-btn\" for=\"collapse-"+id+"\">\n" +
                                "        <i class=\"fas fa-exclamation-triangle\"></i>\n" +
                                "        <span class=\"analysis-warning-title\">" + issue.getMessage() + "</span>\n" +
                                "    </label>\n" +
                                "    <div class=\"collapse-panel\"><div class=\"collapse-inner analysis-detail\">");


                        writer.append(description);

                        writer.append("</div></div>\n" +
                                "</div><code>");
                    }
                    else { //Issue that has only a message

                        writer.append("<div class=\"analysis-warning\">\n" +
                                "    <label class=\"collapse-btn\">\n" +
                                "        <i class=\"fas fa-exclamation-triangle\"></i>\n" +
                                "        <span class=\"analysis-warning-title\">" + issue.getMessage() + "</span>\n"+
                                "    </label>\n");

                        writer.append("</div><code>");
                    }

                }
            }
        }

        IOUtils.copy(sourceFileReader, writer);
        writer.append("</code></pre>");
        return writer.toString();
    }



    @Override
    public String getDisplayName() {
        return issue.getBaseName();
    }

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

