package hudson.plugins.analysis.core;

import java.io.Serializable;

import hudson.model.HealthReport;

import hudson.plugins.analysis.util.model.AnnotationProvider;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Creates a health report for integer values based on healthy and unhealthy
 * thresholds.
 *
 * @see HealthReport
 * @author Ulli Hafner
 */
public class HealthReportBuilder implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5191317904662711835L;
    /** Health descriptor. */
    private final AbstractHealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthReportBuilder}.
     *
     * @param healthDescriptor
     *            health descriptor
     */
    public HealthReportBuilder(final AbstractHealthDescriptor healthDescriptor) {
        this.healthDescriptor = healthDescriptor;
    }

    /**
     * Computes the healthiness of a build based on the specified results.
     * Reports a health of 100% when the specified counter is less than
     * {@link #healthy}. Reports a health of 0% when the specified counter is
     * greater than {@link #unHealthy}. The computation takes only annotations
     * of the specified severity into account.
     *
     * @param result
     *            annotations of the current build
     * @return the healthiness of a build
     */
    public HealthReport computeHealth(final AnnotationProvider result) {
        int numberOfAnnotations = 0;
        for (Priority priority : Priority.collectPrioritiesFrom(healthDescriptor.getMinimumPriority())) {
            numberOfAnnotations += result.getNumberOfAnnotations(priority);
        }

        return computeHealth(numberOfAnnotations, result);
    }

    /**
     * Computes the healthiness of a build based on the specified counter.
     * Reports a health of 100% when the specified counter is less than
     * {@link #healthy}. Reports a health of 0% when the specified counter is
     * greater than {@link #unHealthy}.
     *
     * @param counter
     *            the number of items in a build that should be considered for
     *            health computation
     * @param result
     *            annotations of the current build
     * @return the healthiness of a build
     */
    protected HealthReport computeHealth(final int counter, final AnnotationProvider result) {
        if (healthDescriptor.isHealthyReportEnabled()) {
            int percentage;
            if (counter < healthDescriptor.getHealthyAnnotations()) {
                percentage = 100;
            }
            else if (counter > healthDescriptor.getUnHealthyAnnotations()) {
                percentage = 0;
            }
            else {
                percentage = 100 - ((counter - healthDescriptor.getHealthyAnnotations()) * 100
                        / (healthDescriptor.getUnHealthyAnnotations() - healthDescriptor.getHealthyAnnotations()));
            }
            return new HealthReport(percentage, healthDescriptor.createDescription(result));
        }
        return null;
    }

    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient int healthy;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient int unHealthy;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean isHealthEnabled;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient boolean isThresholdEnabled;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient int threshold;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient String reportName;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient String itemName;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient String reportSingleCount;
    /** Backward compatibility. @deprecated */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @SuppressWarnings("unused")
    @Deprecated
    private transient String reportMultipleCount;
}

