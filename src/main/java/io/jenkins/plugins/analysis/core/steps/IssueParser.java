package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;

/**
 * Parses a file and returns the issues reported in this file.
 *
 * @author Ulli Hafner
 */
public interface IssueParser extends Serializable {
    /**
     * Returns the issues found in the specified file.
     *
     * @param file
     *         the file to parse
     * @param charset
     *         the encoding to use when reading files
     * @param builder
     *         the issue builder to use
     *
     * @return the issues found
     * @throws ParsingException
     *         Signals that during parsing a non recoverable error has been occurred
     * @throws ParsingCanceledException
     *         Signals that the parsing has been aborted by the user
     */
    Issues<Issue> parse(File file, Charset charset, IssueBuilder builder)
            throws ParsingException, ParsingCanceledException;

    /**
     * Returns the ID of this parser. Issues that are reported by this parser will set property {@link
     * Issue#getOrigin()} to this value.
     */
    String getId();

}

