package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.Priority;
import static hudson.plugins.analysis.util.ThresholdValidator.*;

import hudson.plugins.analysis.util.ThresholdValidator;

/**
 * A health descriptor defines the parameters to create the build health.
 *
 * @author Ulli Hafner
 */
public class HealthDescriptor implements Serializable {
    private final String healthy;
    private final String unHealthy;
    private final String minimumPriority;

    public HealthDescriptor(final String healthy, final String unHealthy, final String minimumPriority) {
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.minimumPriority = minimumPriority;
    }

    public HealthDescriptor() {
        this(StringUtils.EMPTY, StringUtils.EMPTY, Priority.NORMAL.toString());
    }

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     * @throws IllegalArgumentException
     *         if the healthy string can't be converted to an integer value greater or equal zero
     */
    public int getHealthy() {
        return ThresholdValidator.convert(healthy);
    }

    /**
     * Returns the unhealthy threshold, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     * @throws IllegalArgumentException
     *         if the unhealthy string can't be converted to an integer value greater or equal zero
     */
    public int getUnHealthy() {
        return ThresholdValidator.convert(unHealthy);
    }

    /**
     * Returns the minimum priority that should be considered when computing build health. E.g., if {@link
     * Priority#NORMAL} is returned, then annotations with priority {@link Priority#LOW} are ignored.
     *
     * @return the minimum priority to consider
     * @throws IllegalArgumentException
     *         if the priority string can't be converted to a priority instance
     */
    public Priority getMinimumPriority() {
        return convert(minimumPriority);
    }

    private Priority convert(final String priority) {
        return Priority.valueOf(StringUtils.upperCase(priority));
    }

    /**
     * Determines whether a health report should be created.
     *
     * @return {@code true} if a health report should be created
     */
    public boolean isEnabled() {
        if (isValid(healthy) && isValid(unHealthy)) {
            int healthyNumber = ThresholdValidator.convert(healthy);
            int unHealthyNumber = ThresholdValidator.convert(unHealthy);

            return unHealthyNumber > healthyNumber;
        }
        return false;
    }
}
