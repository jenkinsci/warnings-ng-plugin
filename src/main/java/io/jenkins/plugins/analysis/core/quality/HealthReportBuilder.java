package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;
import java.util.Map;

import org.jvnet.localizer.Localizable;

import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.analysis.Severity;

import hudson.model.HealthReport;
import hudson.plugins.analysis.Messages;

/**
 * Creates a health report for integer values based on healthy and unhealthy thresholds.
 *
 * @author Ulli Hafner
 * @see HealthReport
 */
public class HealthReportBuilder implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5191317904662711835L;
    /** Health descriptor. */
    private final HealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthReportBuilder}.
     *
     * @param healthDescriptor
     *         health descriptor
     */
    public HealthReportBuilder(final HealthDescriptor healthDescriptor) {
        this.healthDescriptor = healthDescriptor;
    }

    /**
     * Computes the healthiness of a build based on the specified results. Reports a health of 100% when the specified
     * counter is less than {@link HealthDescriptor#getHealthy()}. Reports a health of 0% when the specified counter is
     * greater than {@link HealthDescriptor#getUnHealthy()}. The computation takes only annotations of the specified
     * severity into account.
     *
     * @param sizePerSeverity
     *         number of issues per severity
     *
     * @return the healthiness of a build
     */
    public HealthReport computeHealth(final Map<Severity, Integer> sizePerSeverity) {
        int relevantIssuesSize = 0;
        for (Priority priority : Priority.collectPrioritiesFrom(healthDescriptor.getMinimumPriority())) {
            relevantIssuesSize += sizePerSeverity.getOrDefault(Severity.valueOf(priority), 0);
        }
        relevantIssuesSize += sizePerSeverity.getOrDefault(Severity.ERROR, 0);

        if (healthDescriptor.isEnabled()) {
            int percentage;
            int healthy = healthDescriptor.getHealthy();
            if (relevantIssuesSize < healthy) {
                percentage = 100;
            }
            else {
                int unHealthy = healthDescriptor.getUnHealthy();
                if (relevantIssuesSize > unHealthy) {
                    percentage = 0;
                }
                else {
                    percentage = 100 - ((relevantIssuesSize - healthy) * 100 / (unHealthy - healthy));
                }
            }

            return new HealthReport(percentage, getDescription(relevantIssuesSize));
        }
        return null;
    }


    private Localizable getDescription(final int size) {
        String name = "Static Analysis"; // FIXME: extract from IssueParser.find(id)
        if (size == 0) {
            return Messages._ResultAction_HealthReportNoItem(name);
        }
        else if (size == 1) {
            return Messages._ResultAction_HealthReportSingleItem(name);
        }
        else {
            return Messages._ResultAction_HealthReportMultipleItem(name, size);
        }
    }
}

