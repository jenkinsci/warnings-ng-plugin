package io.jenkins.plugins.analysis.core.quality;

import hudson.model.Result;
import hudson.model.Run;

/**
 * QualityGateStatus of a {@link QualityGate}.
 *
 * @author Ullrich Hafner
 */
public enum QualityGateStatus {
    /** Quality gate has been passed. */
    PASSED(Result.SUCCESS, "fa-check-circle"),

    /** Quality gate has been missed: severity is a warning. */
    WARNING(Result.UNSTABLE, "fa-exclamation-triangle"),

    /** Quality gate has been missed: severity is an error. */
    FAILED(Result.FAILURE, "fa-times-circle"),

    /** Quality gate is inactive, so result evaluation is not available. */
    INACTIVE(Result.NOT_BUILT, "fa-toggle-off");

    private final Result result;
    private final String iconName;

    /**
     * Creates a new {@link QualityGateStatus}.
     *
     * @param result
     *         corresponding result for the build
     * @param iconName
     *         the icon name to show
     */
    QualityGateStatus(final Result result, final String iconName) {
        this.result = result;
        this.iconName = iconName;
    }

    /**
     * Returns the associated icon.
     *
     * @return the icon to use
     */
    public String getIconName() {
        return iconName;
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
     * Returns a description of the status. 
     * 
     * @return a description
     */
    public String getDescription() {
        return result.color.getDescription();
    }
}
