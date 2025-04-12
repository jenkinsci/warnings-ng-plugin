package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;

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
        var model = new PieChartModel(Messages.Severities_Name());

        for (Severity severity : Severity.getPredefinedValues()) {
            int total = report.getSizeOf(severity);
            if (total > 0 || !severity.equals(Severity.ERROR)) {
                model.add(new PieData(LocalizedSeverity.getLocalizedString(severity), total),
                        SeverityPalette.mapToColor(severity).normal());
            }
        }

        return model;
    }
}
