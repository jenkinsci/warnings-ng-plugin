package io.jenkins.plugins.analysis.core.quality;

import hudson.model.BallColor;
import hudson.model.Result;

/**
 * Status of a {@link QualityGate}.
 *
 * @author Ullrich Hafner
 */
public enum Status {
    PASSED(Result.SUCCESS),

    WARNING(Result.UNSTABLE),

    FAILED(Result.FAILURE),

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
