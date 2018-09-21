package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;

import edu.hm.hafner.analysis.Report;

/**
 * Adds SCM information to a set of annotations (e.g., author, commit ID, etc.).
 *
 * @author Lukas Krose
 */
public interface Blamer extends Serializable {
    /**
     * Adds author and commit information to the given report.
     *
     * @param report
     *         the report
     */
    Blames blame(final Report report);
}
