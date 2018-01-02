package io.jenkins.plugins.analysis.core.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;
import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;

import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.util.EncodingValidator;

/**
 * Renders a source file containing an annotation for the whole file or a specific line number.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class SourceDetail implements ModelObject {
    /** Offset of the source code generator. After this line the actual source file lines start. */
    protected static final int SOURCE_GENERATOR_OFFSET = 13;
    /** Color for the first (primary) annotation range. */
    private static final String FIRST_COLOR = "#FCAF3E";
    /** Color for all other annotation ranges. */
    private static final String OTHER_COLOR = "#FCE94F";
    /** The current build as owner of this object. */
    private final Run<?, ?> owner;
    /** Stripped file name of this annotation without the path prefix. */
    private final String fileName;
    /** The annotation to be shown. */
    private final Issue annotation;
    /** The rendered source file. */
    private String sourceCode = StringUtils.EMPTY;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /**
     * Creates a new instance of this source code object.
     *
     * @param owner
     *         the current build as owner of this object
     * @param annotation
     *         the warning to display in the source file
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     */
    public SourceDetail(final Run<?, ?> owner, final Issue annotation, final String defaultEncoding) {
        this.owner = owner;
        this.annotation = annotation;
        this.defaultEncoding = defaultEncoding;
        fileName = StringUtils.substringAfterLast(annotation.getFileName(), "/");

        initializeContent();
    }

    /**
     * Initializes the content of the source file: reads the file, colors it, and splits it into three parts.
     */
    private void initializeContent() {
        InputStream file = null;
        try {
            File tempFile = AffectedFilesResolver.getTempFile(owner, annotation);
            if (tempFile.exists()) {
                file = new FileInputStream(tempFile);
            }
            else {
                file = new FileInputStream(new File(annotation.getFileName()));
            }
            splitSourceFile(highlightSource(file));
        }
        catch (IOException exception) {
            sourceCode = "Can't read file: " + exception.getLocalizedMessage();
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }

    @Override
    public String getDisplayName() {
        return fileName;
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
    public final String highlightSource(final InputStream file) throws IOException {
        JavaSource source = new JavaSourceParser().parse(
                new InputStreamReader(file, EncodingValidator.defaultCharset(defaultEncoding)));

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
    public final void splitSourceFile(final String sourceFile) {
        StringBuilder output = new StringBuilder(sourceFile.length());

        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(sourceFile));

        try {
            int lineNumber = 1;
            while (lineNumber < SOURCE_GENERATOR_OFFSET) {
                copyLine(output, lineIterator);
                lineNumber++;
            }
            lineNumber = 1;
            boolean isFirstRange = true;
            while (lineNumber < annotation.getLineStart()) {
                copyLine(output, lineIterator);
                lineNumber++;
            }
            output.append("</code>\n");
            output.append("</td></tr>\n");
            output.append("<tr><td style=\"background-color:");
            appendRangeColor(output, isFirstRange);
            output.append("\">\n");
            output.append("<div id=\"line");
            output.append(annotation.getLineStart());
            output.append("\" tooltip=\"");
            if (annotation.getLineStart() > 0) {
                outputEscaped(output, annotation.getMessage());
            }
            outputEscaped(output, annotation.getDescription());
            output.append("\" nodismiss=\"\">\n");
            output.append("<code><b>\n");
            if (annotation.getLineStart() <= 0) {
                output.append(annotation.getMessage());
                if (StringUtils.isBlank(annotation.getMessage())) {
                    output.append(annotation.getDescription());
                }
            }
            else {
                while (lineNumber <= annotation.getLineEnd()) {
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
        sourceCode = output.toString();
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
        output.append(StringEscapeUtils.escapeHtml(message));
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
        output.append("\n");
    }

    /**
     * Gets the file name of this source file.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
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

