package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import io.jenkins.plugins.analysis.core.views.LocalizedSeverity;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link PriorityChart}.
 *
 * @author Ullrich Hafner
 */
class PriorityChartTest {
    @Test
    void shouldCreatePriorityChart() {
        PriorityChart chart = new PriorityChart();

        List<AnalysisResult> results = new ArrayList<>();
        results.add(createResult(1, 2, 3, "#1"));
        results.add(createResult(2, 4, 6, "#2"));
        
        LineModel model = chart.create(results);

        assertThatJson(model).node("xAxisLabels")
                .isArray()
                .ofLength(2)
                .thatContains("#1")
                .thatContains("#2");
        
        assertThatJson(model).node("series")
                .isArray()
                .ofLength(3);

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2);

        System.out.println(model);
    }

    private void verifySeries(final LineSeries high, final Severity severity, 
            final int valueFirstBuild, final int valueSecondBuild) {
        assertThatJson(high).node("name")
                .isEqualTo(LocalizedSeverity.getLocalizedString(severity));
        assertThatJson(high).node("data")
                .isArray()
                .ofLength(2)
                .thatContains(valueFirstBuild)
                .thatContains(valueSecondBuild);
    }

    private AnalysisResult createResult(final int high, final int normal, final int low, final String label) {
        AnalysisResult buildResult = mock(AnalysisResult.class);

        when(buildResult.getTotalHighPrioritySize()).thenReturn(high);
        when(buildResult.getTotalNormalPrioritySize()).thenReturn(normal);
        when(buildResult.getTotalLowPrioritySize()).thenReturn(low);

        AnalysisBuild build = mock(AnalysisBuild.class);
        when(build.getDisplayName()).thenReturn(label);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }
}