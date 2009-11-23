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
     * be considered as unstable.
     *
     * @return the threshold of all annotations to be reached if a build should
     *         be considered as unstable.
     */
    String getThreshold();

    /**
     * Returns the threshold for new annotations to be reached if a build should
     * be considered as unstable.
     *
     * @return the threshold for new annotations to be reached if a build should
     *         be considered as unstable.
     */
    String getNewThreshold();

    /**
     * Returns the annotation threshold to be reached if a build should be
     * considered as failure.
     *
     * @return the annotation threshold to be reached if a build should be
     *         considered as failure.
     */
    String getFailureThreshold();

    /**
     * Returns the threshold of new annotations to be reached if a build should
     * be considered as failure.
     *
     * @return the threshold of new annotations to be reached if a build should
     *         be considered as failure.
     */
    String getNewFailureThreshold();

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
     * build health and stability. E.g., if {@link Priority#NORMAL} is returned,
     * then annotations with priority {@link Priority#LOW} are ignored.
     *
     * @return the minimum priority to consider
     */
    Priority getMinimumPriority();
}
