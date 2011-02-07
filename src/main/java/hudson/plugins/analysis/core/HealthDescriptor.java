package hudson.plugins.analysis.core;

import java.io.Serializable;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A health descriptor defines the parameters to create the build health.
 *
 * @author Ulli Hafner
 */
public interface HealthDescriptor extends Serializable {
    /**
     * Returns the threshold of all annotations to be reached if a build should
     * be considered as unstable or failed, resp.
     *
     * @return the threshold
     */
    Thresholds getThresholds();

    /**
     * Returns the healthy threshold, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    String getHealthy();

    /**
     * Returns the unhealthy threshold, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
    String getUnHealthy();

    /**
     * Returns the minimum priority that should be considered when computing
     * build health. E.g., if {@link Priority#NORMAL} is returned, then
     * annotations with priority {@link Priority#LOW} are ignored.
     *
     * @return the minimum priority to consider
     */
    Priority getMinimumPriority();
}
