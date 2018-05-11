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

    ERROR(Result.FAILURE);

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
}
