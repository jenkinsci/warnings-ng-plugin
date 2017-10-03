package io.jenkins.plugins.analysis.core.quality;

/**
 * Defines quality gates for a static analysis run.
 *
 * @author Ullrich Hafner
 */
public class QualityGate {
    private final int failureThreshold;

    public QualityGate(final int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public QualityGate() {
        this(0);
    }

    public boolean hasFailureThreshold() {
        return failureThreshold > 0;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }
}
