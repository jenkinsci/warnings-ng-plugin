package io.jenkins.plugins.analysis.core.charts;

import org.eclipse.collections.impl.factory.Maps;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

import static org.mockito.Mockito.*;

/**
 * Provides factory methods to create {@link AnalysisBuildResult} stubs.
 *
 * @author Ullrich Hafner
 */
final class BuildResultStubs {
    static AnalysisBuildResult createResult(final int buildNumber,
            final int errors, final int high, final int normal, final int low) {
        AnalysisBuildResult buildResult = createBuildResult(buildNumber);

        when(buildResult.getTotalSizeOf(Severity.ERROR)).thenReturn(errors);
        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(low);
        when(buildResult.getTotalSize()).thenReturn(low + normal + high + errors);

        return buildResult;
    }

    static AnalysisBuildResult createResultWithNewIssues(final int buildNumber,
            final int errors, final int high, final int normal, final int low) {
        AnalysisBuildResult buildResult = createBuildResult(buildNumber);

        when(buildResult.getNewSizeOf(Severity.ERROR)).thenReturn(errors);
        when(buildResult.getNewSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getNewSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getNewSizeOf(Severity.WARNING_LOW)).thenReturn(low);
        when(buildResult.getNewSize()).thenReturn(low + normal + high + errors);

        return buildResult;
    }

    static AnalysisBuildResult createResultWithNewAndFixedIssues(final int buildNumber, final int newSize, final int fixedSize) {
        AnalysisBuildResult buildResult = createBuildResult(buildNumber);

        when(buildResult.getFixedSize()).thenReturn(fixedSize);
        when(buildResult.getNewSize()).thenReturn(newSize);

        return buildResult;
    }

    static AnalysisBuildResult createResult(final int buildNumber, final String toolId, final int total) {
        AnalysisBuildResult buildResult = createBuildResult(buildNumber);

        when(buildResult.getSizePerOrigin()).thenReturn(Maps.mutable.of(toolId, total));

        return buildResult;
    }

    private static AnalysisBuildResult createBuildResult(final int buildNumber) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        AnalysisBuild build = createBuild(buildNumber);
        when(buildResult.getBuild()).thenReturn(build);

        return buildResult;
    }

    static AnalysisBuild createBuild(final int buildNumber) {
        return new BuildProperties(buildNumber, "#" + buildNumber, 10);
    }

    private BuildResultStubs() {
        // prevents instantiation
    }
}
