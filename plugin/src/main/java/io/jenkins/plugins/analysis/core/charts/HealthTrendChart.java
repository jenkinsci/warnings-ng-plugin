package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;
import edu.hm.hafner.echarts.Palette;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.echarts.JenkinsPalette;

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
    public LinesChartModel create(final Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            final ChartModelConfiguration configuration) {
        HealthSeriesBuilder builder = new HealthSeriesBuilder(healthDescriptor);
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel(dataSet);

        if (healthDescriptor.isEnabled()) {
            LineSeries healthy = createSeries(Messages.Healthy_Name(), JenkinsPalette.GREEN);
            healthy.addAll(dataSet.getSeries(HealthSeriesBuilder.HEALTHY));
            LineSeries intermediate = createSeries(Messages.Satisfactory_Name(), JenkinsPalette.YELLOW);
            intermediate.addAll(dataSet.getSeries(HealthSeriesBuilder.BETWEEN));
            LineSeries unhealthy = createSeries(Messages.Unhealthy_Name(), JenkinsPalette.RED);
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

    private LineSeries createSeries(final String name, final JenkinsPalette color) {
        return new LineSeries(name, color.normal(), StackedMode.STACKED, FilledMode.FILLED);
    }
}
