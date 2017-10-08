package io.jenkins.plugins.analysis.core.history;

import java.util.NoSuchElementException;
import java.util.Optional;

import io.jenkins.plugins.analysis.core.steps.AnalysisResult;

/**
 * Provides access to the same static analysis results in previous builds.
 *
 * @author Ullrich Hafner
 */
public interface RunResultHistory extends Iterable<AnalysisResult> {
    /**
     * Returns the previous result.
     *
     * @return the previous result
     * @throws NoSuchElementException
     *             if there is no previous result
     */
    Optional<AnalysisResult> getPreviousResult();

    /**
     * Returns the baseline result.
     *
     * @return the baseline result
     * @throws NoSuchElementException if the baseline does not contain a result
     */
    AnalysisResult getBaseline();
}
