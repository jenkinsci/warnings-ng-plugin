package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.Status;

/**
 * Provides access to the static analysis results of a predefined type in the history of builds of a job.
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
     * Returns the previous result (if there is any). Note that the quality gate {@link Status} is not taken into
     * account when selecting this result.
     *
     * @return the previous result
     */
    Optional<AnalysisResult> getPreviousResult();
}
