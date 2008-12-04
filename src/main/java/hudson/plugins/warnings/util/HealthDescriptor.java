package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.Priority;

import java.io.Serializable;


/**
 * A health descriptor defines the parameters to create the build health. It
 * consists of the following constraints:
 * <ul>
 * <li>A flag to determine whether a build should be marked unstable if the
 * number of annotations reaches a given threshold.</li>
 * <li>A flag to determine whether a build should change its healthiness
 * according to the number of warnings. The healthiness is interpolated between
 * the specified minimum and maximum values.</li>
 *
 * @author Ulli Hafner
 */
public interface HealthDescriptor extends Serializable {
    /**
     * Determines whether a threshold has been defined.
     *
     * @return <code>true</code> if a threshold has been defined
     */
    boolean isThresholdEnabled();

    /**
     * Returns the threshold to be reached if a build should be considered as
     * unstable.
     *
     * @return the threshold to be reached if a build should be considered as
     *         unstable
     */
    int getMinimumAnnotations();

    /**
     * Determines whether a health report should be created.
     *
     * @return <code>true</code> if a health report should be created
     */
    boolean isHealthyReportEnabled();

    /**
     * Returns the healthy threshold for annotations, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     */
    int getHealthyAnnotations();

    /**
     * Returns the unhealthy threshold of annotations, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     */
    int getUnHealthyAnnotations();

    /**
     * Returns the minimum priority that should be considered when computing
     * build health and stability. E.g., if {@link Priority#NORMAL} is
     * returned, then annotations with priority {@link Priority#LOW} are
     * ignored.
     *
     * @return the minimum priority to consider
     */
    Priority getMinimumPriority();
}
