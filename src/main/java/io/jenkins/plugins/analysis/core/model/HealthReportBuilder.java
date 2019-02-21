package io.jenkins.plugins.analysis.core.model;

import java.util.Map;

import com.google.errorprone.annotations.CheckReturnValue;

import edu.hm.hafner.analysis.Severity;

import hudson.model.HealthReport;

import io.jenkins.plugins.analysis.core.util.HealthDescriptor;

/**
 * Creates a health report for integer values based on healthy and unhealthy thresholds.
 *
 * @author Ullrich Hafner
 * @see HealthReport
 */
public class HealthReportBuilder {
    /**
     * Computes the healthiness of a build based on the specified results. Reports a health of 100% when the specified
     * counter is less than {@link HealthDescriptor#getHealthy()}. Reports a health of 0% when the specified counter is
     * greater than {@link HealthDescriptor#getUnhealthy()}. The computation takes only annotations of the specified
     * severity into account.
     *
     * @param healthDescriptor
     *         health report configuration
     * @param labelProvider
     *         label provider to get the messages from
     * @param sizePerSeverity
     *         number of issues per severity
     *
     * @return the healthiness of a build
     */
    @CheckReturnValue
    HealthReport computeHealth(final HealthDescriptor healthDescriptor,
            final StaticAnalysisLabelProvider labelProvider,
            final Map<Severity, Integer> sizePerSeverity) {
        int relevantIssuesSize = 0;
        for (Severity severity : Severity.collectSeveritiesFrom(healthDescriptor.getMinimumSeverity())) {
            relevantIssuesSize += sizePerSeverity.getOrDefault(severity, 0);
        }
        relevantIssuesSize += sizePerSeverity.getOrDefault(Severity.ERROR, 0);

        if (healthDescriptor.isValid()) {
            int percentage;
            int healthy = healthDescriptor.getHealthy();
            if (relevantIssuesSize < healthy) {
                percentage = 100;
            }
            else {
                int unhealthy = healthDescriptor.getUnhealthy();
                if (relevantIssuesSize > unhealthy) {
                    percentage = 0;
                }
                else {
                    percentage = 100 - ((relevantIssuesSize - healthy) * 100 / (unhealthy - healthy));
                }
            }

            return new HealthReport(percentage, labelProvider.getToolTipLocalizable(relevantIssuesSize));
        }
        return null;
    }
}

