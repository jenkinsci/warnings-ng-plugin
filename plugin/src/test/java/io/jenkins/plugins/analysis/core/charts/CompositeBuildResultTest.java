package io.jenkins.plugins.analysis.core.charts;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static io.jenkins.plugins.analysis.core.charts.BuildResultStubs.*;

/**
 * Tests the class {@link CompositeBuildResult}.
 *
 * @author Ullrich Hafner
 */
class CompositeBuildResultTest {
    @Test
    void shouldMergeNumberOfIssuesByOrigin() {
        AnalysisBuildResult first = createAnalysisBuildResult("first", 1);
        AnalysisBuildResult second = createAnalysisBuildResult("second", 2);

        CompositeBuildResult run = new CompositeBuildResult(List.of());

        assertThat(run.getSizePerOrigin()).isEmpty();
        assertThat(new CompositeBuildResult(List.of(first)).getSizePerOrigin()).containsExactly(entry("first", 1));
        assertThat(new CompositeBuildResult(List.of(first, second)).getSizePerOrigin()).containsExactly(entry("first", 1), entry("second", 2));
    }

    @Test
    void shouldMergeNumberOfTotalIssues() {
        AnalysisBuildResult first = createAnalysisBuildResult(1, 2, 3, 4);
        AnalysisBuildResult second = createAnalysisBuildResult(5, 6, 7, 8);

        CompositeBuildResult run = new CompositeBuildResult(List.of());

        assertThat(run).hasTotalSize(0);
        assertThat(new CompositeBuildResult(List.of(first))).hasTotalSize(10);
        assertThat(new CompositeBuildResult(List.of(first, second))).hasTotalSize(36);
    }

    @Test
    void shouldMergeNumberOfFixedIssues() {
        AnalysisBuildResult first = createAnalysisBuildResultWithNewAndFixedIssues(0, 2);
        AnalysisBuildResult second = createAnalysisBuildResultWithNewAndFixedIssues(0, 3);

        CompositeBuildResult run = new CompositeBuildResult(List.of());

        assertThat(run).hasFixedSize(0);
        assertThat(new CompositeBuildResult(List.of(first))).hasFixedSize(2);
        assertThat(new CompositeBuildResult(List.of(first, second))).hasFixedSize(5);
    }

    @Test
    void shouldMergeNumberOfNewIssues() {
        AnalysisBuildResult first = createAnalysisBuildResultWithNew(0, 5, 2, 6);
        AnalysisBuildResult second = createAnalysisBuildResultWithNew(0, 4, 2, 7);

        CompositeBuildResult run = new CompositeBuildResult(List.of());

        assertThat(run).hasNewSize(0);
        assertThat(new CompositeBuildResult(List.of(first))).hasNewSize(13);
        assertThat(new CompositeBuildResult(List.of(first, second))).hasNewSize(26);
    }

    @Test
    void shouldMergeTotalNumberOfIssuesBySeverity() {
        AnalysisBuildResult first = createAnalysisBuildResult(7, 1, 2, 3);
        AnalysisBuildResult second = createAnalysisBuildResult(8, 4, 5, 6);

        CompositeBuildResult run = new CompositeBuildResult(List.of());

        assertThat(run.getTotalSizeOf(Severity.ERROR)).isEqualTo(0);
        assertThat(run.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(0);
        assertThat(run.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(0);
        assertThat(run.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(0);

        run = new CompositeBuildResult(List.of(first));
        assertThat(run.getTotalSizeOf(Severity.ERROR)).isEqualTo(7);
        assertThat(run.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(1);
        assertThat(run.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(2);
        assertThat(run.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(3);

        run = new CompositeBuildResult(List.of(first, second));
        assertThat(run.getTotalSizeOf(Severity.ERROR)).isEqualTo(15);
        assertThat(run.getTotalSizeOf(Severity.WARNING_HIGH)).isEqualTo(5);
        assertThat(run.getTotalSizeOf(Severity.WARNING_NORMAL)).isEqualTo(7);
        assertThat(run.getTotalSizeOf(Severity.WARNING_LOW)).isEqualTo(9);
    }

    @Test
    void shouldMergeNumberOfNewIssuesBySeverity() {
        AnalysisBuildResult first = createAnalysisBuildResultWithNew(7, 2, 6, 2);
        AnalysisBuildResult second = createAnalysisBuildResultWithNew(8, 5, 2, 2);

        CompositeBuildResult run = new CompositeBuildResult(List.of());

        assertThat(run.getNewSizeOf(Severity.ERROR)).isEqualTo(0);
        assertThat(run.getNewSizeOf(Severity.WARNING_HIGH)).isEqualTo(0);
        assertThat(run.getNewSizeOf(Severity.WARNING_NORMAL)).isEqualTo(0);
        assertThat(run.getNewSizeOf(Severity.WARNING_LOW)).isEqualTo(0);

        run = new CompositeBuildResult(List.of(first));
        assertThat(run.getNewSizeOf(Severity.ERROR)).isEqualTo(7);
        assertThat(run.getNewSizeOf(Severity.WARNING_HIGH)).isEqualTo(2);
        assertThat(run.getNewSizeOf(Severity.WARNING_NORMAL)).isEqualTo(6);
        assertThat(run.getNewSizeOf(Severity.WARNING_LOW)).isEqualTo(2);

        run = new CompositeBuildResult(List.of(first, second));
        assertThat(run.getNewSizeOf(Severity.ERROR)).isEqualTo(15);
        assertThat(run.getNewSizeOf(Severity.WARNING_HIGH)).isEqualTo(7);
        assertThat(run.getNewSizeOf(Severity.WARNING_NORMAL)).isEqualTo(8);
        assertThat(run.getNewSizeOf(Severity.WARNING_LOW)).isEqualTo(4);
    }
}
