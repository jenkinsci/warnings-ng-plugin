package io.jenkins.plugins.analysis.core;

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
     * Returns the new number of issues in this analysis run.
     *
     * @return new number of issues
     */
    int getNewSize();

    /**
     * Returns the new number of high priority issues in this analysis run.
     *
     * @return new number of high priority issues
     */
    int getNewHighPrioritySize();

    /**
     * Returns the new number of normal priority issues in this analysis run.
     *
     * @return new number of normal priority issues
     */
    int getNewNormalPrioritySize();

    /**
     * Returns the new number of low priority issues in this analysis run.
     *
     * @return new number of low priority of issues
     */
    int getNewLowPrioritySize();
}
