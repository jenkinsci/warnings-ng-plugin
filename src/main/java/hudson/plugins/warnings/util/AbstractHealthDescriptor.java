package hudson.plugins.warnings.util;

import static hudson.plugins.warnings.util.ThresholdValidator.*;
import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.Priority;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

/**
 * A base class for serializable health descriptors. Instances of this class are
 * immutable.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractHealthDescriptor implements HealthDescriptor {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -3709673381162699834L;
    /** The minimum priority to consider during health and stability calculation. */
    private final Priority priority;
    /** Annotation threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Threshold for new annotations to be reached if a build should be considered as unstable. */
    private final String newThreshold;
    /** Annotation threshold to be reached if a build should be considered as failure. */
    private final String failureThreshold;
    /** Threshold for new annotations to be reached if a build should be considered as failure. */
    private final String newFailureThreshold;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;

    /**
     * Creates a new instance of {@link AbstractHealthDescriptor} based on the
     * values of the specified descriptor.
     *
     * @param healthDescriptor the descriptor to copy the values from
     */
    public AbstractHealthDescriptor(final HealthDescriptor healthDescriptor) {
        priority = healthDescriptor.getMinimumPriority();
        threshold = healthDescriptor.getThreshold();
        newThreshold = healthDescriptor.getNewThreshold();
        failureThreshold = healthDescriptor.getFailureThreshold();
        newFailureThreshold = healthDescriptor.getNewFailureThreshold();
        healthy = healthDescriptor.getHealthy();
        unHealthy = healthDescriptor.getUnHealthy();
    }

    /**
     * Creates a new instance of {@link AbstractHealthDescriptor}.
     */
    public AbstractHealthDescriptor() {
        threshold = StringUtils.EMPTY;
        newThreshold = StringUtils.EMPTY;
        failureThreshold = StringUtils.EMPTY;
        newFailureThreshold = StringUtils.EMPTY;
        healthy = StringUtils.EMPTY;
        unHealthy = StringUtils.EMPTY;
        priority = Priority.LOW;
    }

    /** {@inheritDoc} */
    public Priority getMinimumPriority() {
        return priority;
    }

    /** {@inheritDoc} */
    public String getThreshold() {
        return threshold;
    }

    /** {@inheritDoc} */
    public String getNewThreshold() {
        return newThreshold;
    }

    /** {@inheritDoc} */
    public String getFailureThreshold() {
        return failureThreshold;
    }

    /** {@inheritDoc} */
    public String getNewFailureThreshold() {
        return newFailureThreshold;
    }

    /** {@inheritDoc} */
    public String getHealthy() {
        return healthy;
    }

    /** {@inheritDoc} */
    public String getUnHealthy() {
        return unHealthy;
    }

    /**
     * Returns whether this health report build is enabled, i.e. at least one of
     * the health or failed thresholds are provided.
     *
     * @return <code>true</code> if health or failed thresholds are provided,
     *         <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return isHealthyReportEnabled() || isThresholdEnabled();
    }

    /**
     * Returns a localized description of the build health.
     *
     * @param result
     *            the result of the build
     * @return a localized description of the build health
     */
    protected abstract Localizable createDescription(final AnnotationProvider result);

    /**
     * Determines whether a threshold has been defined.
     *
     * @return <code>true</code> if a threshold has been defined
     */
    public boolean isThresholdEnabled() {
        return isValid(threshold);
    }

    /**
     * Returns the threshold to be reached if a build should be considered as
     * unstable.
     *
     * @return the threshold to be reached if a build should be considered as
     *         unstable
     */
    public int getMinimumAnnotations() {
        if (isThresholdEnabled()) {
            return convert(threshold);
        }
        throw new IllegalArgumentException("Threshold is not valid: " + threshold);
    }

    /**
     * Determines whether a health report should be created.
     *
     * @return <code>true</code> if a health report should be created
     */
    public boolean isHealthyReportEnabled() {
        if (isValid(healthy) && isValid(unHealthy)) {
            int healthyNumber = convert(healthy);
            int unHealthyNumber = convert(unHealthy);

            return unHealthyNumber > healthyNumber;
        }
        return false;
    }

    /**
     * Returns the healthy threshold for annotations, i.e. when health is reported as 100%.
     *
     * @return the 100% healthiness
     * @throws IllegalArgumentException if the healthy values are not valid
     * @see #isHealthyReportEnabled()
     */
    public int getHealthyAnnotations() {
        if (isHealthyReportEnabled()) {
            return convert(healthy);
        }
        throw new IllegalArgumentException("Healthy values are not valid: " + healthy + ", " + unHealthy);
    }

    /**
     * Returns the unhealthy threshold of annotations, i.e. when health is reported as 0%.
     *
     * @return the 0% unhealthiness
     * @throws IllegalArgumentException if the healthy values are not valid
     * @see #isHealthyReportEnabled()
     */
    public int getUnHealthyAnnotations() {
        if (isHealthyReportEnabled()) {
            return convert(unHealthy);
        }
        throw new IllegalArgumentException("Healthy values are not valid: " + healthy + ", " + unHealthy);
    }

    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean isFailureThresholdEnabled;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int minimumAnnotations;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int healthyAnnotations;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int unHealthyAnnotations;
    /** Backward compatibility. */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean isHealthyReportEnabled;
}

