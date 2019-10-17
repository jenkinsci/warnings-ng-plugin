package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.charts.CompositeResult.CompositeAnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static io.jenkins.plugins.analysis.core.charts.BuildResultStubs.*;

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
        resultsCheckStyle.add(createResult(2, 0, 2, 4, 6));
        resultsCheckStyle.add(createResult(1, 0, 1, 2, 3));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(2, 0, 12, 14, 16));
        resultsSpotBugs.add(createResult(1, 0, 11, 12, 13));

        CompositeResult compositeResult = new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs));

        assertThat(compositeResult.iterator()).toIterable().hasSize(2);

        Iterator<AnalysisBuildResult> iterator = compositeResult.iterator();

        AnalysisBuildResult first = iterator.next();
        assertThat(first).hasBuild(createBuild(2));
        assertThat(first.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(14);
        assertThat(first.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(18);
        assertThat(first.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(22);

        AnalysisBuildResult second = iterator.next();
        assertThat(second).hasBuild(createBuild(1));
        assertThat(second.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(12);
        assertThat(second.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(14);
        assertThat(second.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(16);
    }

    @Test
    void shouldCreatePriorityChartForJobAndMultipleActions() {
        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(2, CHECK_STYLE, 2));
        resultsCheckStyle.add(createResult(1, CHECK_STYLE, 1));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(2, SPOT_BUGS, 4));
        resultsSpotBugs.add(createResult(1, SPOT_BUGS, 3));

        CompositeResult compositeResult = new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs));

        assertThat(compositeResult.iterator()).toIterable().hasSize(2);

        Iterator<AnalysisBuildResult> iterator = compositeResult.iterator();
        AnalysisBuildResult first = iterator.next();
        AnalysisBuildResult second = iterator.next();

        assertThat(first).hasBuild(createBuild(2));
        assertThat(second).hasBuild(createBuild(1));

        assertThat(first.getSizePerOrigin()).contains(entry(CHECK_STYLE, 2), entry(SPOT_BUGS, 4));
        assertThat(second.getSizePerOrigin()).contains(entry(CHECK_STYLE, 1), entry(SPOT_BUGS, 3));
    }

    private List<Iterable<? extends AnalysisBuildResult>> asList(final List<AnalysisBuildResult> resultsCheckStyle,
            final List<AnalysisBuildResult> resultsSpotBugs) {
        return Arrays.asList(resultsCheckStyle, resultsSpotBugs);
    }

    /**
     * Tests the class {@link CompositeAnalysisBuildResult}.
     */
    @Nested
    class CompositeAnalysisBuildResultTest {
        @Test
        void shouldMergeNumberOfTotalIssues() {
            AnalysisBuildResult first = createResult(1, 0, 3, 1, 6);
            AnalysisBuildResult second = createResult(1, 0, 1, 3, 9);

            CompositeAnalysisBuildResult run = new CompositeAnalysisBuildResult(first, second);

            assertThat(run).hasBuild(createBuild(1));
            assertThat(run).hasTotalSize(23);
        }

        @Test
        void shouldMergeNumberOfFixedIssues() {
            AnalysisBuildResult first = createResultWithNewAndFixedIssues(1, 0, 2);
            AnalysisBuildResult second = createResultWithNewAndFixedIssues(1, 0, 3);

            CompositeAnalysisBuildResult run = new CompositeAnalysisBuildResult(first, second);

            assertThat(run).hasBuild(createBuild(1));
            assertThat(run.getFixedSize()).isEqualTo(5);
        }

        @Test
        void shouldMergeNumberOfNewIssues() {
            AnalysisBuildResult first = createResultWithNewIssues(1, 0, 5, 2, 6);
            AnalysisBuildResult second = createResultWithNewIssues(1, 0, 4, 2, 7);

            CompositeAnalysisBuildResult run = new CompositeAnalysisBuildResult(first, second);

            assertThat(run).hasBuild(createBuild(1));
            assertThat(run).hasNewSize(26);
        }

        @Test
        void shouldMergeTotalNumberOfIssuesBySeverity() {
            AnalysisBuildResult first = createResult(1, 0, 1, 2, 3);
            AnalysisBuildResult second = createResult(1, 0, 4, 5, 6);

            CompositeAnalysisBuildResult run = new CompositeAnalysisBuildResult(first, second);

            assertThat(run).hasBuild(createBuild(1));
            assertThat(run.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(5);
            assertThat(run.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(7);
            assertThat(run.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(9);
        }

        @Test
        void shouldMergeNumberOfNewIssuesBySeverity() {
            AnalysisBuildResult first = createResultWithNewIssues(1, 0, 2, 6, 2);
            AnalysisBuildResult second = createResultWithNewIssues(1, 0, 5, 2, 2);

            CompositeAnalysisBuildResult run = new CompositeAnalysisBuildResult(first, second);

            assertThat(run).hasBuild(createBuild(1));
            assertThat(run.getNewSizeOf(Severity.WARNING_HIGH)).isEqualTo(7);
            assertThat(run.getNewSizeOf(Severity.WARNING_NORMAL)).isEqualTo(8);
            assertThat(run.getNewSizeOf(Severity.WARNING_LOW)).isEqualTo(4);
        }
    }
}
