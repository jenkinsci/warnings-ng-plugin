package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.charts.CompositeResult.CompositeAnalysisBuildResult;
import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link CompositeResult}.
 *
 * @author Ullrich Hafner
 */
class CompositeResultTest {
    private static final String CHECK_STYLE = "checkStyle";
    private static final String SPOT_BUGS = "spotBugs";

    @Test
    void shouldCreateResultOfSequenceWithIdenticalBuilds() {
        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(1, 2, 3, 1));
        resultsCheckStyle.add(createResult(2, 4, 6, 2));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(11, 12, 13, 1));
        resultsSpotBugs.add(createResult(12, 14, 16, 2));

        CompositeResult compositeResult = new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs));

        assertThat(compositeResult.iterator()).toIterable().hasSize(2);

        Iterator<AnalysisBuildResult> iterator = compositeResult.iterator();
        AnalysisBuildResult first = iterator.next();
        AnalysisBuildResult second = iterator.next();

        assertThat(first).hasBuild(createBuild(1));
        assertThat(second).hasBuild(createBuild(2));

        assertThat(first.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(12);
        assertThat(first.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(14);
        assertThat(first.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(16);

        assertThat(second.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(14);
        assertThat(second.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(18);
        assertThat(second.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(22);
    }

    @Test
    void shouldCreatePriorityChartForJobAndMultipleActions() {
        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(1, CHECK_STYLE, 1));
        resultsCheckStyle.add(createResult(2, CHECK_STYLE, 2));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(1, SPOT_BUGS, 3));
        resultsSpotBugs.add(createResult(2, SPOT_BUGS, 4));

        CompositeResult compositeResult = new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs));

        assertThat(compositeResult.iterator()).toIterable().hasSize(2);

        Iterator<AnalysisBuildResult> iterator = compositeResult.iterator();
        AnalysisBuildResult first = iterator.next();
        AnalysisBuildResult second = iterator.next();

        assertThat(first).hasBuild(createBuild(1));
        assertThat(second).hasBuild(createBuild(2));

        assertThat(first.getSizePerOrigin()).contains(entry(CHECK_STYLE, 1), entry(SPOT_BUGS, 3));
        assertThat(second.getSizePerOrigin()).contains(entry(CHECK_STYLE, 2), entry(SPOT_BUGS, 4));
    }

    private List<Iterable<? extends AnalysisBuildResult>> asList(final List<AnalysisBuildResult> resultsCheckStyle,
            final List<AnalysisBuildResult> resultsSpotBugs) {
        return Arrays.asList(resultsCheckStyle, resultsSpotBugs);
    }

    private AnalysisBuildResult createResult(final int high, final int normal, final int low, final int number) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(low);

        AnalysisBuild build = createBuild(number);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }

    private AnalysisBuildResult createResult(final int buildNumber, final String toolId, final int total) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getSizePerOrigin()).thenReturn(Maps.mutable.of(toolId, total));

        AnalysisBuild build = new BuildProperties(buildNumber, "#" + buildNumber, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }

    private AnalysisBuild createBuild(final int number) {
        return new BuildProperties(number, "#" + number, 10);
    }

    /**
     * Tests the class {@link CompositeAnalysisBuildResult}.
     */
    @Nested
    class CompositeAnalysisBuildResultTest {
        @Test
        void shouldTestMerge() {
            AnalysisBuildResult first = createResult(1, 2, 3, 1);
            AnalysisBuildResult second = createResult(4, 5, 6, 1);

            CompositeAnalysisBuildResult run = new CompositeAnalysisBuildResult(first, second);

            assertThat(run).hasBuild(createBuild(1));
            assertThat(run.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(5);
            assertThat(run.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(7);
            assertThat(run.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(9);
        }
    }
}