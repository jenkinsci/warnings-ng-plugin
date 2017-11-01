package io.jenkins.plugins.analysis.core.quality;

import java.util.Map;

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
