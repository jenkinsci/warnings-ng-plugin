package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.io.input.BOMInputStream;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public abstract class StreamBasedParser extends StaticAnalysisTool {
    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return parse(createParser(), file, charset, builder);
    }

    /**
     * Creates the parser for this static analyis tool.
     *
     * @return the parser
     */
    protected abstract AbstractParser createParser();

    /**
     * Parses the specified file for issues.
     *
     * @param parser
     *         the parser to use
     * @param file
     *         the file to parse
     * @param charset
     *         the encoding to use when reading files
     * @param builder
     *         the issue builder to use
     *
     * @return the parsed issues
     * @throws ParsingException
     *         Signals that during parsing a non recoverable error has been occurred
     * @throws ParsingCanceledException
     *         Signals that the parsing has been aborted by the user
     */
    public Issues<Issue> parse(final AbstractParser parser, final File file, final Charset charset, final IssueBuilder builder)
            throws ParsingException, ParsingCanceledException {
        try (Reader input = createReader(new FileInputStream(file), charset)) {
            Issues<Issue> issues = parser.parse(input, builder);
            issues.log("Successfully parsed '%s': found %d issues (tool ID = %s)",
                    file.getAbsolutePath(), issues.getSize(), getId());
            if (issues.getDuplicatesSize() == 1) {
                issues.log("Note: one issue has been dropped since it is a duplicate");
            }
            else if (issues.getDuplicatesSize() > 1) {
                issues.log("Note: %d issues have been dropped since they are duplicates",
                        issues.getDuplicatesSize());
            }
            return issues;
        }
        catch (FileNotFoundException exception) {
            throw new ParsingException(exception, "Can't find file: " + file.getAbsolutePath());
        }
        catch (IOException exception) {
            throw new ParsingException(exception, "Can't scan file for issues: " + file.getAbsolutePath());
        }
    }

    private Reader createReader(final InputStream inputStream, final Charset charset) {
        return new InputStreamReader(new BOMInputStream(inputStream), charset);
    }
}
