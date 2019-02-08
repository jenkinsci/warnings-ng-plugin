package io.jenkins.plugins.analysis.core.util;

import hudson.model.BallColor;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator.FormattedLogger;

/**
 * Result of a {@link QualityGateEvaluator#evaluate(IssuesStatistics, FormattedLogger)} call.
 *
 * @author Ullrich Hafner
 */
public enum QualityGateStatus {
    /** Quality gate is inactive, so result evaluation is not available. */
    INACTIVE(Result.NOT_BUILT),

    /** Quality gate has been passed. */
    PASSED(Result.SUCCESS),

    /** Quality gate has been missed: severity is a warning. */
    WARNING(Result.UNSTABLE),

    /** Quality gate has been missed: severity is an error. */
    FAILED(Result.FAILURE);

    private final Result result;

    QualityGateStatus(final Result result) {
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

    /**
     * Sets the result of the specified run to the associated value of this quality gate status.
     *
     * @param run
     *         the run to set the result for
     */
    public void setResult(final Run<?, ?> run) {
        if (!isSuccessful()) {
            run.setResult(result);
        }
    }

    /**
     * Returns whether this status is worse than the specified status.
     *
     * @param other
     *         the other status
     *
     * @return {@code true} if this status is worse than the other status, {@code false} otherwise
     */
    public boolean isWorseThan(final QualityGateStatus other) {
        return ordinal() > other.ordinal();
    }
}
