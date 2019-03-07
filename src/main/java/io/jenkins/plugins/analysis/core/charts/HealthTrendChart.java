package io.jenkins.plugins.analysis.core.charts;

import io.jenkins.plugins.analysis.core.charts.LineSeries.FilledMode;
import io.jenkins.plugins.analysis.core.charts.LineSeries.StackedMode;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the model for a trend chart showing all issues for a given number of builds. The issues are colored according
 * to the specified health report.
 *
 * @author Ullrich Hafner
 */
public class HealthTrendChart implements TrendChart {
    private final HealthDescriptor healthDescriptor;

    /**
     * Creates a new chart with the specified {@link HealthDescriptor}.
     *
     * @param healthDescriptor
     *         determines the range between healthy and unhealthy values
     */
    public HealthTrendChart(final HealthDescriptor healthDescriptor) {
        this.healthDescriptor = healthDescriptor;
    }

    @Override
    public LinesChartModel create(final Iterable<? extends StaticAnalysisRun> results,
            final ChartModelConfiguration configuration) {
        HealthSeriesBuilder builder = new HealthSeriesBuilder(healthDescriptor);
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel();
        model.addXAxisLabels(dataSet.getXAxisLabels());

        if (healthDescriptor.isEnabled()) {
            LineSeries healthy = createSeries(Messages.Healthy_Name(), Palette.GREEN);
            healthy.addAll(dataSet.getSeries(HealthSeriesBuilder.HEALTHY));
            LineSeries intermediate = createSeries(Messages.Satisfactory_Name(), Palette.YELLOW);
            intermediate.addAll(dataSet.getSeries(HealthSeriesBuilder.BETWEEN));
            LineSeries unhealthy = createSeries(Messages.Unhealthy_Name(), Palette.RED);
            unhealthy.addAll(dataSet.getSeries(HealthSeriesBuilder.UNHEALTHY));
            model.addSeries(healthy, intermediate, unhealthy);
        }
        else {
            LineSeries total = new LineSeries(Messages.Total_Name(), Palette.YELLOW.getNormal(),
                    StackedMode.SEPARATE_LINES, FilledMode.LINES);
            total.addAll(dataSet.getSeries(HealthSeriesBuilder.TOTAL));
            model.addSeries(total);
        }

        return model;
    }

    private LineSeries createSeries(final String name, final Palette color) {
        return new LineSeries(name, color.getNormal(), StackedMode.STACKED, FilledMode.FILLED);
    }
}
