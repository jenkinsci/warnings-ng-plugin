package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the model for a graph showing all issues by severity.
 *
 * @author Ullrich Hafner
 */
public class SeverityChart {
    private static final int MAX_BUILDS = 50;

    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the analysis results to render
     *
     * @return the chart model
     */
    // TODO: make chart configurable
    public LineModel create(final Iterable<? extends StaticAnalysisRun> results) {
        LineSeries high = createSeries(Severity.WARNING_HIGH);
        LineSeries normal = createSeries(Severity.WARNING_NORMAL);
        LineSeries low = createSeries(Severity.WARNING_LOW);

        LineModel model = new LineModel();
        model.addSeries(low, normal, high);

        for (StaticAnalysisRun result : results) {
            high.add(result.getTotalSizeOf(Severity.WARNING_HIGH));
            normal.add(result.getTotalSizeOf(Severity.WARNING_NORMAL));
            low.add(result.getTotalSizeOf(Severity.WARNING_LOW));

            model.addXAxisLabel(result.getBuild().getDisplayName()); // TODO: de-normalize
            if (model.size() > MAX_BUILDS) {
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
