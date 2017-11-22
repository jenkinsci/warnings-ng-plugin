package io.jenkins.plugins.analysis.core.quality;

/**
 * Defines quality gates for a static analysis run.
 *
 * @author Ullrich Hafner
 */
public class QualityGate {
    private final int failureThreshold;

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param failureThreshold
     *         the number of issues that will fail a build
     */
    public QualityGate(final int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    /**
     * Creates a new instance of {@link QualityGate}. No thresholds are set.
     */
    public QualityGate() {
        this(0);
    }

    /**
     * Determines if a failure threshold for the total number of issues is set.
     *
     * @return {@code true} if the failure threshold for the total number of issues is set
     */
    public boolean hasFailureThreshold() {
        return failureThreshold > 0;
    }

    /**
     * Returns the failure threshold for the total number of issues.
     *
     * @return the failure threshold for the total number of issues
     */
    public int getFailureThreshold() {
        return failureThreshold;
    }
}
