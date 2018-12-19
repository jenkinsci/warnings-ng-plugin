package io.jenkins.plugins.analysis.core.util;

/**
 * Represents the build that a static analysis run was part of.
 *
 * @author Ullrich Hafner
 */
public interface AnalysisBuild extends Comparable<AnalysisBuild> {
    /**
     * Returns the start time value of this build in milliseconds.
     *
     * @return the time as UTC milliseconds from the epoch
     */
    long getTimeInMillis();

    /**
     * Returns the number of this build as assigned by Jenkins' scheduler.
     *
     * @return the number of this build
     */
    int getNumber();

    /**
     * Returns a human readable label for this build.
     *
     * @return the name to be used in the user interface
     */
    String getDisplayName();
}
