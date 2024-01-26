package io.jenkins.plugins.analysis.core.charts;

import org.eclipse.collections.impl.factory.Maps;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.util.VisibleForTesting;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;

import static org.mockito.Mockito.*;

/**
 * Provides factory methods to create {@link AnalysisBuildResult} stubs.
 *
 * @author Ullrich Hafner
 */
public final class BuildResultStubs {
    static BuildResult<AnalysisBuildResult> createResult(final int buildNumber,
            final int errors, final int high, final int normal, final int low) {
        AnalysisBuildResult buildResult = createAnalysisBuildResult(errors, high, normal, low);

        return createBuildResult(buildNumber, buildResult);
    }

    static AnalysisBuildResult createAnalysisBuildResult(final int errors, final int high, final int normal,
            final int low) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getTotalSizeOf(Severity.ERROR)).thenReturn(errors);
        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(low);
        when(buildResult.getTotalSize()).thenReturn(low + normal + high + errors);

        var statistics = new IssuesStatisticsBuilder();
        statistics.setTotalErrorSize(errors);
        statistics.setTotalHighSize(high);
        statistics.setTotalNormalSize(normal);
        statistics.setTotalLowSize(low);

        when(buildResult.getTotals()).thenReturn(statistics.build());

        return buildResult;
    }

    @VisibleForTesting
    static BuildResult<AnalysisBuildResult> createBuildResult(
            final int buildNumber, final AnalysisBuildResult result) {
        return new BuildResult<>(new Build(buildNumber), result);
    }

    static BuildResult<AnalysisBuildResult> createResultWithNewIssues(final int buildNumber,
            final int errors, final int high, final int normal, final int low) {
        AnalysisBuildResult buildResult = createAnalysisBuildResultWithNew(errors, high, normal, low);

        return createBuildResult(buildNumber, buildResult);
    }

    static AnalysisBuildResult createAnalysisBuildResultWithNew(final int errors, final int high, final int normal,
            final int low) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getNewSizeOf(Severity.ERROR)).thenReturn(errors);
        when(buildResult.getNewSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getNewSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getNewSizeOf(Severity.WARNING_LOW)).thenReturn(low);
        when(buildResult.getNewSize()).thenReturn(low + normal + high + errors);

        var statistics = new IssuesStatisticsBuilder();
        statistics.setNewErrorSize(errors);
        statistics.setNewHighSize(high);
        statistics.setNewNormalSize(normal);
        statistics.setNewLowSize(low);

        when(buildResult.getTotals()).thenReturn(statistics.build());

        return buildResult;
    }

    static BuildResult<AnalysisBuildResult> createResultWithNewAndFixedIssues(
            final int buildNumber, final int newSize, final int fixedSize) {
        AnalysisBuildResult buildResult = createAnalysisBuildResultWithNewAndFixedIssues(newSize, fixedSize);

        return createBuildResult(buildNumber, buildResult);
    }

    static AnalysisBuildResult createAnalysisBuildResultWithNewAndFixedIssues(final int newSize, final int fixedSize) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getFixedSize()).thenReturn(fixedSize);
        when(buildResult.getNewSize()).thenReturn(newSize);

        var statistics = new IssuesStatisticsBuilder();
        statistics.setFixedSize(fixedSize);
        statistics.setNewNormalSize(newSize);

        when(buildResult.getTotals()).thenReturn(statistics.build());

        return buildResult;
    }

    static AnalysisBuildResult createAnalysisBuildResult(final String toolId, final int total) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getSizePerOrigin()).thenReturn(Maps.mutable.of(toolId, total));
        when(buildResult.getTotals()).thenReturn(new IssuesStatisticsBuilder().build());

        return buildResult;
    }

    private BuildResultStubs() {
        // prevents instantiation
    }
}
