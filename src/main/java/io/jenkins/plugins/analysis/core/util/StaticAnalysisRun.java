package io.jenkins.plugins.analysis.core.util;

import java.util.Map;
import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

import edu.hm.hafner.analysis.Severity;

import hudson.model.Run;

/**
 * Provides detailed information for the results of a static analysis run.
 */
public interface StaticAnalysisRun {
    /**
     * Returns the run that created this static analysis result.
     *
     * @return the run
     */
    Run<?, ?> getOwner();

    /**
     * Returns the error messages of the analysis run.
     *
     * @return the error messages
     */
    ImmutableList<String> getErrorMessages();

    /**
     * Returns the info messages of the analysis run.
     *
     * @return the info messages
     */
    ImmutableList<String> getInfoMessages();

    /**
     * Returns the build number since the associated job has a successful static analysis result.
     *
     * @return the build number since the static analysis result is successful, or -1 if the result is not successful
     */
    int getSuccessfulSinceBuild();

    /**
     * Returns the {@link QualityGateStatus} of the {@link QualityGateEvaluator} evaluation of the static analysis run.
     *
     * @return the quality gate status
     */
    QualityGateStatus getQualityGateStatus();

    /**
     * Returns the reference static analysis run that has been used to compute the new issues.
     *
     * @return the reference build
     */
    Optional<Run<?, ?>> getReferenceBuild();

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
     * Returns the build number since the associated job has no issues.
     *
     * @return the build number since there are no issues, or -1 if issues have been reported
     */
    int getNoIssuesSinceBuild();

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
