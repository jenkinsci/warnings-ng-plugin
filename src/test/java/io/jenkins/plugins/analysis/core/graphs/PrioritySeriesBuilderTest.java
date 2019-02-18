package io.jenkins.plugins.analysis.core.graphs;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link PrioritySeriesBuilder}.
 *
 * @author Ullrich Hafner
 */
class PrioritySeriesBuilderTest {
    /** Verifies that an empty list of builds produces no data. */
    @Test
    void shouldHaveEmptyDataSetForEmptyIterator() {
        PrioritySeriesBuilder builder = new PrioritySeriesBuilder();

        LinesChartModel dataSet = builder.createDataSet(createConfiguration(), Lists.newArrayList());

        assertThat(dataSet.getXSize()).isEqualTo(0);
        assertThat(dataSet.getDataSetSize()).isEqualTo(0);
    }

    private ChartModelConfiguration createConfiguration() {
        return mock(ChartModelConfiguration.class);
    }

    /**
     * Verifies that a list with one build result produces one column with rows containing the correct number of issues
     * per priority.
     */
    @Test
    void shouldHaveThreeValuesForSingleBuild() {
        PrioritySeriesBuilder builder = new PrioritySeriesBuilder();

        StaticAnalysisRun singleResult = createBuildResult(1,
                1, 2, 3);

        LinesChartModel dataSet = builder.createDataSet(createConfiguration(), Lists.newArrayList(singleResult));

        assertThat(dataSet.getXSize()).isEqualTo(1);
        assertThat(dataSet.getXLabel(0)).isEqualTo("#1");

        assertThat(dataSet.getDataSetSize()).isEqualTo(3);

        assertThat(dataSet.getValue("High", 0)).isEqualTo(1);
        assertThat(dataSet.getValue("Normal", 0)).isEqualTo(2);
        assertThat(dataSet.getValue("Low", 0)).isEqualTo(3);
    }

    private StaticAnalysisRun createBuildResult(final int buildNumber, final int numberOfHighPriorityIssues,
            final int numberOfNormalPriorityIssues, final int numberOfLowPriorityIssues) {
        StaticAnalysisRun buildResult = mock(StaticAnalysisRun.class);

        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(numberOfHighPriorityIssues);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(numberOfNormalPriorityIssues);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(numberOfLowPriorityIssues);

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