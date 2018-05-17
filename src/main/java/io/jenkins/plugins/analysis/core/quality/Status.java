package io.jenkins.plugins.analysis.core.quality;

import hudson.model.BallColor;
import hudson.model.Result;

/**
 * Status of a {@link QualityGate}.
 *
 * @author Ullrich Hafner
 */
public enum Status {
    /** Quality gate has been passed. */
    PASSED(Result.SUCCESS),

    /** Quality gate has been missed: severity is a warning. */
    WARNING(Result.UNSTABLE),

    /** Quality gate has been missed: severity is an error. */
    FAILED(Result.FAILURE),

    /** Quality gate is inactive, so result evaluation is not available. */
    INACTIVE(Result.NOT_BUILT);

    private Result result;

    Status(final Result result) {
        this.result = result;
    }

    /**
     * Returns the associated {@link Result} color.
     *
     * @return Jenkins' {@link Result} color
     */
    public BallColor getColor() {
        return result.color;
    }

    /**
     * Returns whether the quality gate has been passed (or has not been activated at all).
     *
     * @return {@code true} if the quality gate has been passed, {@code false}  otherwise
     */
    public boolean isSuccessful() {
        return this == PASSED || this == INACTIVE;
    }
}
