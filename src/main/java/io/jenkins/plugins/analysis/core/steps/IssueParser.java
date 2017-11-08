package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;

/**
 * Parses a file and returns the issues reported in this file.
 *
 * @author Ulli Hafner
 */
// FIXME: encoding?
public interface IssueParser extends Serializable {
    /**
     * Returns the issues found in the specified file.
     *
     * @param file
     *         the file to parse
     * @param builder
     *         the issue builder to use
     *
     * @return the issues found
     * @throws InvocationTargetException
     *         if the file could not be parsed (wrap your exception in this exception)
     */
    // FIXME: why dont use ParserException?
    Issues<Issue> parse(File file, IssueBuilder builder) throws InvocationTargetException;

    /**
     * Return the ID of this parser.
     *
     * @return ID of the parser
     */
    String getId();
}

