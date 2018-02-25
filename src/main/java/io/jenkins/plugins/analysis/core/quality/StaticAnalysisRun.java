package io.jenkins.plugins.analysis.core.quality;

import java.util.Map;

import org.kohsuke.stapler.export.Exported;

import edu.hm.hafner.analysis.Priority;

import hudson.model.Result;

/**
 * Describes the results of a static analysis run.
 *
 * @author Ullrich Hafner
 */
public interface StaticAnalysisRun {
    /**
     * Returns the total number of issues in this analysis run.
     *
     * @return total number of issues
     */
    int getTotalSize();

    /**
     * Returns the total number of issues in this analysis run, that have the specified {@link Priority}.
     *
     * @param priority
     *         the priority of the issues to match
     *
     * @return total number of issues
     */
    int getTotalSize(Priority priority);

    /**
     * Returns the total number of high priority issues in this analysis run.
     *
     * @return total number of high priority issues
     */
    int getTotalHighPrioritySize();

    /**
     * Returns the total number of normal priority issues in this analysis run.
     *
     * @return total number of normal priority issues
     */
    int getTotalNormalPrioritySize();

    /**
     * Returns the total number of low priority issues in this analysis run.
     *
     * @return total number of low priority of issues
     */
    int getTotalLowPrioritySize();

    /**
     * Returns the number of new issues in this analysis run.
     *
     * @return number of new issues
     */
    int getNewSize();

    /**
     * Returns the number of new high priority issues in this analysis run.
     *
     * @return number of new high priority issues
     */
    int getNewHighPrioritySize();

    /**
     * Returns the number of new normal priority issues in this analysis run.
     *
     * @return number of new normal priority issues
     */
    int getNewNormalPrioritySize();

    /**
     * Returns the number of new low priority issues in this analysis run.
     *
     * @return number of new low priority of issues
     */
    int getNewLowPrioritySize();

    /**
     * Returns the number of fixed issues in this analysis run.
     *
     * @return number of fixed issues
     */
    int getFixedSize();

    QualityGate getQualityGate();

    /**
     * Returns the build number since the associated job has no issues.
     *
     * @return the build number since there are no issues, or -1 if issues have been reported
     */
    @Exported
    int getNoIssuesSinceBuild();

    /**
     * Returns the build number since the associated job has a successful static analysis result.
     *
     * @return the build number since the static analysis result is successful, or -1 if the result is
     *         not successful
     */
    @Exported
    int getSuccessfulSinceBuild();

    /**
     * Returns the {@link Result} of the static analysis run.
     *
     * @return the static analysis result
     */
    @Exported
    Result getOverallResult();

    int getReferenceBuild();

    /**
     * Returns the number of issues in this analysis run, mapped by their origin.
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
}
