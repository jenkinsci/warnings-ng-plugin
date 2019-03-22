package io.jenkins.plugins.analysis.core.util;

import java.util.Map;

import edu.hm.hafner.analysis.Severity;

/**
 * Provides statistics for the results of a static analysis run.
 */
public interface AnalysisBuildResult {
    /**
     * Returns the number of issues in this analysis run, mapped by their origin. The origin is the tool that created
     * the report.
     *
     * @return number of issues per origin
     */
    Map<String, Integer> getSizePerOrigin();

    /**
     * Returns the associated build that this run was part of.
     *
     * @return the associated build
     */
    AnalysisBuild getBuild();

    /**
     * Returns the number of fixed issues in this analysis run.
     *
     * @return number of fixed issues
     */
    int getFixedSize();

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
