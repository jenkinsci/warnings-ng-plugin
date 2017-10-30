package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import edu.hm.hafner.analysis.Issues;

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
     *            the file to parse
     * @param moduleName
     *            name of the project module
     * @return the issues found
     * @throws InvocationTargetException
     *             if the file could not be parsed (wrap your exception in this exception)
     */
    Issues parse(File file, String moduleName) throws InvocationTargetException;
}

