package io.jenkins.plugins.analysis.core.history;

import java.util.NoSuchElementException;

import io.jenkins.plugins.analysis.core.steps.BuildResult;

/**
 * Provides access to the same static analysis results in previous builds.
 *
 * @author Ullrich Hafner
 */
public interface RunResultHistory extends Iterable<BuildResult> {
    /**
     * Returns whether a previous result exists.
     *
     * @return {@code true} if a previous result exists.
     * @see #isEmpty()
     */
    boolean hasPrevious();

    /**
     * Returns whether there is no history available, i.e. the baseline is
     * the only valid one.
     *
     * @return {@code true}  if there is no previous result available
     * @see #hasPrevious()
     */
    boolean isEmpty();

    /**
     * Returns the previous result.
     *
     * @return the previous result
     * @see #hasPrevious()
     * @throws NoSuchElementException
     *             if there is no previous result
     */
    BuildResult getPrevious();

    /**
     * Returns the baseline result.
     *
     * @return the baseline result
     */
    BuildResult getBaseline();
}
