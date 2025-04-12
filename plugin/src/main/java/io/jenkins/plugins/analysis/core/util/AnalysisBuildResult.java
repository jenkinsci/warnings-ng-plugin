package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.analysis.Severity;

import java.util.Map;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

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
    @Whitelisted
    Map<String, Integer> getSizePerOrigin();

    /**
     * Returns the number of fixed issues in this analysis run.
     *
     * @return number of fixed issues
     */
    @Whitelisted
    int getFixedSize();

    /**
     * Returns the total number of issues in this analysis run.
     *
     * @return total number of issues
     */
    @Whitelisted
    int getTotalSize();

    /**
     * Returns the total number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    @Whitelisted
    int getTotalSizeOf(Severity severity);

    /**
     * Returns the number of new issues in this analysis run.
     *
     * @return number of new issues
     */
    @Whitelisted
    int getNewSize();

    /**
     * Returns the new number of issues in this analysis run that have the specified {@link Severity}.
     *
     * @param severity
     *         the severity of the issues to match
     *
     * @return total number of issues
     */
    @Whitelisted
    int getNewSizeOf(Severity severity);

    /**
     * Returns the total number of issues (by severity, new, total, fixed and delta) in a build.
     *
     * @return the totals
     */
    @Whitelisted
    IssuesStatistics getTotals();
}
