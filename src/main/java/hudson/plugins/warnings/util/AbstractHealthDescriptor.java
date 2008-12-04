package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.Priority;

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
    /** Determines whether to use the provided threshold to mark a build as unstable. */
    private final boolean isFailureThresholdEnabled;
    /** Integer threshold to be reached if a build should be considered as unstable. */
    private final int minimumAnnotations;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final int healthyAnnotations;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final int unHealthyAnnotations;
    /** Determines whether to use the provided healthy thresholds. */
    private final boolean isHealthyReportEnabled;
    /** The minimum priority to consider during health and stability calculation. */
    private final Priority priority;

    /**
     * Creates a new instance of {@link AbstractHealthDescriptor} based on the
     * values of the specified descriptor.
     *
     * @param healthDescriptor the descriptor to copy the values from
     */
    public AbstractHealthDescriptor(final HealthDescriptor healthDescriptor) {
        isFailureThresholdEnabled = healthDescriptor.isThresholdEnabled();
        minimumAnnotations = healthDescriptor.getMinimumAnnotations();
        isHealthyReportEnabled = healthDescriptor.isHealthyReportEnabled();
        healthyAnnotations = healthDescriptor.getHealthyAnnotations();
        unHealthyAnnotations = healthDescriptor.getUnHealthyAnnotations();
        priority = healthDescriptor.getMinimumPriority();
    }

    /**
     * Creates a new instance of {@link AbstractHealthDescriptor}.
     */
    public AbstractHealthDescriptor() {
        isFailureThresholdEnabled = false;
        isHealthyReportEnabled = false;
        minimumAnnotations = 0;
        healthyAnnotations = 0;
        unHealthyAnnotations = 0;
        priority = Priority.LOW;
    }

    /** {@inheritDoc} */
    public int getHealthyAnnotations() {
        return healthyAnnotations;
    }

    /** {@inheritDoc} */
    public int getMinimumAnnotations() {
        return minimumAnnotations;
    }

    /** {@inheritDoc} */
    public int getUnHealthyAnnotations() {
        return unHealthyAnnotations;
    }

    /** {@inheritDoc} */
    public boolean isHealthyReportEnabled() {
        return isHealthyReportEnabled;
    }

    /** {@inheritDoc} */
    public boolean isThresholdEnabled() {
        return isFailureThresholdEnabled;
    }

    /** {@inheritDoc} */
    public Priority getMinimumPriority() {
        return priority;
    }

    /**
     * Returns a localized description of the build health.
     *
     * @param result
     *            the result of the build
     * @return a localized description of the build health
     */
    protected abstract Localizable createDescription(final AnnotationProvider result);
}

