package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.LocalizedSeverity;

/**
 * Builds the model for a graph showing all issues by severity.
 *
 * @author Ullrich Hafner
 */
public class SeverityChart {
    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the analysis results to render
     *
     * @return the chart model
     */
    // TODO: make chart configurable
    public LineModel create(final Iterable<? extends AnalysisResult> results) {
        LineSeries high = createSeries(Severity.WARNING_HIGH);
        LineSeries normal = createSeries(Severity.WARNING_NORMAL);
        LineSeries low = createSeries(Severity.WARNING_LOW);

        LineModel model = new LineModel();
        model.addSeries(low, normal, high);

        for (AnalysisResult result : results) {
            high.add(result.getTotalHighPrioritySize());
            normal.add(result.getTotalNormalPrioritySize());
            low.add(result.getTotalLowPrioritySize());

            model.addXAxisLabel(result.getBuild().getDisplayName()); // TODO: de-normalize
            if (model.size() > 50) {
                break;
            }
        }

        return model;
    }

    private LineSeries createSeries(final Severity severity) {
        return new LineSeries(LocalizedSeverity.getLocalizedString(severity), 
                SeverityPalette.getColor(severity).getNormal());
    }
}
