package io.jenkins.plugins.analysis.core.util;

import java.io.Serializable;

import edu.hm.hafner.analysis.Severity;

/**
 * A health descriptor defines the parameters to create the build health.
 *
 * @author Ullrich Hafner
 */
public class HealthDescriptor implements Serializable {
    private static final long serialVersionUID = -2509226746813680432L;

    private final int healthy;
    private final int unhealthy;
    private final Severity minimumSeverity;

    /**
     * Creates a new {@link HealthDescriptor}.
     *
     * @param healthy
     *         the healthy threshold, i.e. when health is reported as 100%.
     * @param unhealthy
     *         the unhealthy threshold, i.e. when health is reported as 0%.
     * @param minimumSeverity
     *         the minimum severity that should be considered when computing build health
     */
    public HealthDescriptor(final int healthy, final int unhealthy, final Severity minimumSeverity) {
        this.healthy = healthy;
        this.unhealthy = unhealthy;
        this.minimumSeverity = minimumSeverity;
    }

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    public int getHealthy() {
        return healthy;
    }

    /**
     * Returns the unhealthy threshold, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
    public int getUnhealthy() {
        return unhealthy;
    }

    /**
     * Returns the minimum severity that should be considered when computing build health. E.g., if {@link
     * Severity#WARNING_NORMAL} is returned, then annotations with severity {@link Severity#WARNING_LOW} are ignored.
     *
     * @return the minimum severity to consider
     */
    public Severity getMinimumSeverity() {
        return minimumSeverity;
    }

    /**
     * Determines whether health reporting is enabled.
     *
     * @return {@code true} if  health reporting is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return healthy > 0 || unhealthy > 0;
    }

    /**
     * Determines whether health reporting is enabled.
     *
     * @return {@code true} if  health reporting is enabled, {@code false} otherwise
     */
    public boolean isValid() {
        return healthy > 0 && unhealthy > healthy;
    }

    @Override
    public String toString() {
        return String.format("Healthy=%d, Unhealthy=%d, Minimum Severity=%s", healthy, unhealthy, minimumSeverity);
    }
}
