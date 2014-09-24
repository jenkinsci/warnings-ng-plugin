package hudson.plugins.analysis.core;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import static hudson.plugins.analysis.util.ThresholdValidator.*;

import hudson.plugins.analysis.util.model.AnnotationProvider;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A base class for serializable health descriptors. Instances of this class are
 * immutable.
 *
 * @author Ulli Hafner
 */
@ExportedBean
public abstract class AbstractHealthDescriptor implements HealthDescriptor {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -3709673381162699834L;
    /** The minimum priority to consider during health and stability calculation. */
    private final Priority priority;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** Build status thresholds. */
    private Thresholds thresholds;

    /**
     * Creates a new instance of {@link AbstractHealthDescriptor} based on the
     * values of the specified descriptor.
     *
     * @param healthDescriptor the descriptor to copy the values from
     */
    public AbstractHealthDescriptor(final HealthDescriptor healthDescriptor) {
        priority = healthDescriptor.getMinimumPriority();
        healthy = healthDescriptor.getHealthy();
        unHealthy = healthDescriptor.getUnHealthy();
        thresholds = healthDescriptor.getThresholds();
    }

    /**
     * Creates a new instance of {@link AbstractHealthDescriptor}.
     */
    public AbstractHealthDescriptor() {
        thresholds = new Thresholds();
        healthy = StringUtils.EMPTY;
        unHealthy = StringUtils.EMPTY;
        priority = Priority.LOW;
    }

    @Override @Exported
    public Priority getMinimumPriority() {
        return priority;
    }

    @Override @Exported
    public String getHealthy() {
        return healthy;
    }

    @Override @Exported
    public String getUnHealthy() {
        return unHealthy;
    }

    @Override @Exported
    public Thresholds getThresholds() {
        return thresholds;
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
        return thresholds.isValid();
    }

    /**
     * Returns a lower bound of warnings that will guarantee that a build
     * neither is unstable or failed.
     *
     * @return the number of warnings
     */
    public int getLowerBoundOfThresholds() {
        return thresholds.getLowerBound();
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
        throw createException();
    }

    /**
     * Creates a new {@link IllegalArgumentException}.
     *
     * @return a new {@link IllegalArgumentException}
     */
    private IllegalArgumentException createException() {
        return new IllegalArgumentException("Healthy values are not valid: " + healthy + ", " + unHealthy);
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
        throw createException();
    }

    /**
     * Initializes new fields that are not serialized yet.
     *
     * @return the object
     */
    protected Object readResolve() {
        if (thresholds == null) {
            thresholds = new Thresholds();

            if (threshold != null) {
                thresholds.unstableTotalAll = threshold;
                threshold = null; // NOPMD
            }
            if (newThreshold != null) {
                thresholds.unstableNewAll = newThreshold;
                newThreshold = null; // NOPMD
            }
            if (failureThreshold != null) {
                thresholds.failedTotalAll = failureThreshold;
                failureThreshold = null; //NOPMD
            }
            if (newFailureThreshold != null) {
                thresholds.failedNewAll = newFailureThreshold;
                newFailureThreshold = null; // NOPMD
            }
        }
        return this;
    }

    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String threshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String newThreshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String failureThreshold;
    /** Backward compatibility. @deprecated */
    @Deprecated
    private transient String newFailureThreshold;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean isFailureThresholdEnabled;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int minimumAnnotations;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int healthyAnnotations;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("unused")
    @Deprecated
    private transient int unHealthyAnnotations;
    /** Backward compatibility. @deprecated */
    @SuppressWarnings("all")
    @Deprecated
    private transient boolean isHealthyReportEnabled;
}

