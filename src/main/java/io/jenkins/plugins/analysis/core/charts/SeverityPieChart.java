package io.jenkins.plugins.analysis.core.charts;

import org.eclipse.collections.api.set.ImmutableSet;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;

/**
 * Builds the model for a pie chart showing the distribution of issues by severity.
 *
 * @author Ullrich Hafner
 */
public class SeverityPieChart {
    /**
     * Creates the chart for the specified result.
     *
     * @param report
     *         the report to render
     *
     * @return the chart model
     */
    public PieChartModel create(final Report report) {
        PieChartModel model = new PieChartModel("Severities Distribution");

        ImmutableSet<Severity> predefinedSeverities = Severity.getPredefinedValues();
        for (Severity severity : predefinedSeverities) {
            model.add(new PieModel(LocalizedSeverity.getLocalizedString(severity), report.getSizeOf(severity)),
                    SeverityPalette.getColor(severity));
        }

        return model;
    }
}
