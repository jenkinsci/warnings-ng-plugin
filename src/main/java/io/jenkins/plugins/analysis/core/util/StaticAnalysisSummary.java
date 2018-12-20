package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.analysis.Severity;

/**
 * Provides a summary of a static analysis run. I.e., the number of issues (totals and new issues).
 */
public interface StaticAnalysisSummary {
    /**
     * Returns the total number of issues in this analysis run.
     *
     * @return total number of issues
     */
    int getTotalSize();

    /**
     * Returns the total number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    int getTotalSizeOf(Severity severity);

    /**
     * Returns the number of new issues in this analysis run.
     *
     * @return number of new issues
     */
    int getNewSize();

    /**
     * Returns the new number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    int getNewSizeOf(Severity severity);
}
