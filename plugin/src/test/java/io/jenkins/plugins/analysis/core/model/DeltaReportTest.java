package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import java.util.Optional;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.IssuesStatisticsAssert;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;

import static io.jenkins.plugins.analysis.core.model.DeltaReportAssert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DeltaReport}.
 *
 * @author Andreas Pabst
 */
class DeltaReportTest {
    private static final String REFERENCE_BUILD_ID = "refBuildId";

    @Test
    void shouldHaveEmptyReports() {
        History history = mock(History.class);
        when(history.getBuild()).thenReturn(Optional.empty());
        var report = new Report();

        var deltaReport = new DeltaReport(report, 0);
        assertThat(deltaReport)
                .isEmpty()
                .hasNoAllIssues()
                .hasNoOutstandingIssues()
                .hasNoNewIssues()
                .hasNoFixedIssues()
                .hasReferenceBuildId(StringUtils.EMPTY);
    }

    @Test
    void shouldHaveCorrectReports() {
        Run<?, ?> run = mock(Run.class);
        when(run.getExternalizableId()).thenReturn(REFERENCE_BUILD_ID);

        var issue = getIssue("issue");
        var fixedIssue = getIssue("fixedIssue");
        var newIssue = getIssue("newIssue");

        var referenceIssues = new Report();
        referenceIssues.add(issue);
        referenceIssues.add(fixedIssue);

        var report = new Report();
        report.add(issue);
        report.add(newIssue);

        var deltaReport = new DeltaReport(report, run, 0, referenceIssues);
        assertThat(deltaReport)
                .hasAllIssues(issue, newIssue)
                .hasOutstandingIssues(issue)
                .hasNewIssues(newIssue)
                .hasFixedIssues(fixedIssue)
                .hasReferenceBuildId(REFERENCE_BUILD_ID);
    }

    @Test
    void shouldCreateIssuesStatistics() {
        Run<?, ?> run = mock(Run.class);
        when(run.getExternalizableId()).thenReturn(REFERENCE_BUILD_ID);

        History history = mock(History.class);
        when(history.getBuild()).thenReturn(Optional.of(run));

        var issue = getIssue("issue");
        var fixedIssue = getIssue("fixedIssue");
        var newIssue = getIssue("newIssue");
        var error = getIssueWithSeverity("error", Severity.ERROR);

        var referenceIssues = new Report();
        referenceIssues.add(issue);
        referenceIssues.add(fixedIssue);
        when(history.getIssues()).thenReturn(referenceIssues);

        var report = new Report();
        report.add(issue);
        report.add(newIssue);
        report.add(error);

        var compareIssuesStatistics = new IssuesStatisticsBuilder()
                .setTotalNormalSize(2)
                .setTotalErrorSize(1)
                .setNewNormalSize(1)
                .setNewErrorSize(1)
                .setDeltaErrorSize(1)
                .setFixedSize(1)
                .build();

        var deltaReport = new DeltaReport(report, run, 0, referenceIssues);
        var issuesStatistics = deltaReport.getStatistics();
        IssuesStatisticsAssert.assertThat(issuesStatistics)
                .isNotNull().usingRecursiveComparison()
                .isEqualTo(compareIssuesStatistics);
    }

    private Issue getIssue(final String name) {
        try (var builder = new IssueBuilder()) {
            return builder.setFileName(name).setFingerprint(name).build();
        }
    }

    private Issue getIssueWithSeverity(final String name, final Severity severity) {
        try (var builder = new IssueBuilder()) {
            return builder.setFileName(name).setFingerprint(name).setSeverity(severity).build();
        }
    }
}
