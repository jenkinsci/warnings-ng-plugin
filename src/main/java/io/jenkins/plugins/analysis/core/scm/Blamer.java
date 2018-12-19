package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;

import edu.hm.hafner.analysis.Report;

/**
 * Obtains SCM information for a report of issues (e.g., author, commit ID, etc.).
 *
 * @author Lukas Krose
 */
public interface Blamer extends Serializable {
    /**
     * Obtains author and commit information for all issues of the given report.
     *
     * @param report
     *         the report
     *
     * @return the blames
     */
    Blames blame(Report report);
}
