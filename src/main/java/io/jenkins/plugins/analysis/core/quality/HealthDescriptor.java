package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.Priority;
import static hudson.plugins.analysis.util.ThresholdValidator.*;

/**
 * A health descriptor defines the parameters to create the build health.
 *
 * @author Ulli Hafner
 */
public class HealthDescriptor implements Serializable {
    private final String healthy;
    private final String unHealthy;
    private final Priority minimumPriority;

    public HealthDescriptor(final String healthy, final String unHealthy, final Priority minimumPriority) {
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.minimumPriority = minimumPriority;
    }

    public HealthDescriptor() {
        this(StringUtils.EMPTY, StringUtils.EMPTY, Priority.NORMAL);
    }

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     * @throws IllegalArgumentException
     *         if the healthy string can't be converted to an integer value greater or equal zero
     */
    public int getHealthy() {
        return convert(healthy);
    }

    /**
     * Returns the unhealthy threshold, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     * @throws IllegalArgumentException
     *         if the unhealthy string can't be converted to an integer value greater or equal zero
     */
    public int getUnHealthy() {
        return convert(unHealthy);
    }

    /**
     * Returns the minimum priority that should be considered when computing build health. E.g., if {@link
     * Priority#NORMAL} is returned, then annotations with priority {@link Priority#LOW} are ignored.
     *
     * @return the minimum priority to consider
     */
    public Priority getMinimumPriority() {
        return minimumPriority;
    }

    /**
     * Determines whether a health report should be created.
     *
     * @return {@code true} if a health report should be created
     */
    public boolean isEnabled() {
        if (isValid(healthy) && isValid(unHealthy)) {
            int healthyNumber = convert(healthy);
            int unHealthyNumber = convert(unHealthy);

            return unHealthyNumber > healthyNumber;
        }
        return false;
    }
}
