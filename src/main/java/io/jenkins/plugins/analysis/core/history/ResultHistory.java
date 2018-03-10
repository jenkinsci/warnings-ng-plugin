package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Provides access to the same static analysis results in the history of runs of a job.
 *
 * @author Ullrich Hafner
 */
public interface ResultHistory extends Iterable<AnalysisResult> {
    /**
     * Returns the baseline result (if already available).
     *
     * @return the baseline result
     */
    Optional<AnalysisResult> getBaselineResult();

    /**
     * Returns the previous result (if there is one with plugin result SUCCESS).
     *
     * @return the previous result
     */
    // FIXME: currently the status must be SUCCESS!
    Optional<AnalysisResult> getPreviousResult();
}
