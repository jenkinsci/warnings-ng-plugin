package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import io.jenkins.plugins.analysis.core.charts.ChartModelConfiguration.AxisType;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

import static edu.hm.hafner.analysis.Severity.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SeveritySeriesBuilder}.
 *
 * @author Ullrich Hafner
 */
class SeveritySeriesBuilderTest {
    /** Verifies that an empty list of builds produces no data. */
    @Test
    void shouldHaveEmptyDataSetForEmptyIterator() {
        SeveritySeriesBuilder builder = new SeveritySeriesBuilder();

        LinesDataSet model = builder.createDataSet(createConfiguration(), Lists.newArrayList());

        assertThat(model.getXAxisSize()).isEqualTo(0);
        assertThat(model.getDataSetIds()).isEmpty();
    }

    private ChartModelConfiguration createConfiguration() {
        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        return configuration;
    }

    /**
     * Verifies that a list with one build result produces one column with rows containing the correct number of issues
     * per priority.
     */
    @Test
    void shouldHaveThreeValuesForSingleBuild() {
        SeveritySeriesBuilder builder = new SeveritySeriesBuilder();

        StaticAnalysisRun singleResult = createBuildResult(1,
                1, 2, 3);

        LinesDataSet dataSet = builder.createDataSet(createConfiguration(), Lists.newArrayList(singleResult));

        assertThat(dataSet.getXAxisSize()).isEqualTo(1);
        assertThat(dataSet.getXAxisLabels()).containsExactly("#1");

        assertThat(dataSet.getDataSetIds()).containsExactlyInAnyOrder(
                ERROR.getName(), WARNING_HIGH.getName(), WARNING_NORMAL.getName(), WARNING_LOW.getName());

        assertThat(dataSet.getSeries(WARNING_HIGH.getName())).containsExactly(1);
        assertThat(dataSet.getSeries(WARNING_NORMAL.getName())).containsExactly(2);
        assertThat(dataSet.getSeries(WARNING_LOW.getName())).containsExactly(3);
        assertThat(dataSet.getSeries(ERROR.getName())).containsExactly(0);
    }

    private StaticAnalysisRun createBuildResult(final int buildNumber, final int numberOfHighPriorityIssues,
            final int numberOfNormalPriorityIssues, final int numberOfLowPriorityIssues) {
        StaticAnalysisRun buildResult = mock(StaticAnalysisRun.class);

        when(buildResult.getTotalSizeOf(WARNING_HIGH)).thenReturn(numberOfHighPriorityIssues);
        when(buildResult.getTotalSizeOf(WARNING_NORMAL)).thenReturn(numberOfNormalPriorityIssues);
        when(buildResult.getTotalSizeOf(WARNING_LOW)).thenReturn(numberOfLowPriorityIssues);

        AnalysisBuild build = createRun(buildNumber);
        when(buildResult.getBuild()).thenReturn(build);

        return buildResult;
    }

    private AnalysisBuild createRun(final int number) {
        AnalysisBuild run = mock(AnalysisBuild.class);
        when(run.getNumber()).thenReturn(number);
        when(run.getDisplayName()).thenReturn("#" + number);
        return run;
    }
}