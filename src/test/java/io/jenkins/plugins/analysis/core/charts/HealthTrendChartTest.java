package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;

import static java.util.Arrays.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;


/**
 * Tests the class {@link HealthTrendChart}.
 *
 * @author Matthias Herpers
 */

class HealthTrendChartTest {

    /**
     * Verifies that a HealthTrendChart with two Results shows right x/Axis labels.
     */
    @Test
    void shouldCreateHealthChartForJobAndMultipleActions() {
        int healty = 5;
        int unhelthy = 10;
        Severity minSeverity = new Severity("TestSeverity");
        HealthDescriptor healthDescriptor = new HealthDescriptor(healty, unhelthy, minSeverity);
        HealthTrendChart chart = new HealthTrendChart(healthDescriptor);

        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(1));
        resultsCheckStyle.add(createResult(2));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(1));
        resultsSpotBugs.add(createResult(2));

        LinesChartModel model = chart.create(
                new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs)), new ChartModelConfiguration());

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("series")
                .isArray().hasSize(3);
    }

    /**
     * Creates Mock object.
     * @param number number of Build.
     * @return
     */
    private AnalysisBuildResult createResult(final int number) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);
        AnalysisBuild build = new BuildProperties(number, "#" + number, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }


}
