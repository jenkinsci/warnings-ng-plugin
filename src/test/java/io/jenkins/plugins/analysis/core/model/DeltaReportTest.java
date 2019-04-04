package io.jenkins.plugins.analysis.core.model;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics;
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
        Report report = new Report();

        DeltaReport deltaReport = new DeltaReport(report, history, 0);
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

        History history = mock(History.class);
        when(history.getBuild()).thenReturn(Optional.of(run));

        Issue issue = getIssue("issue");
        Issue fixedIssue = getIssue("fixedIssue");
        Issue newIssue = getIssue("newIssue");

        Report referenceIssues = new Report();
        referenceIssues.add(issue);
        referenceIssues.add(fixedIssue);
        when(history.getIssues()).thenReturn(referenceIssues);

        Report report = new Report();
        report.add(issue);
        report.add(newIssue);

        DeltaReport deltaReport = new DeltaReport(report, history, 0);
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

        Issue issue = getIssue("issue");
        Issue fixedIssue = getIssue("fixedIssue");
        Issue newIssue = getIssue("newIssue");
        Issue newIssue2 = getIssueWithSeverity("newIssue2", Severity.ERROR);

        Report referenceIssues = new Report();
        referenceIssues.add(issue);
        referenceIssues.add(fixedIssue);
        when(history.getIssues()).thenReturn(referenceIssues);

        Report report = new Report();
        report.add(issue);
        report.add(newIssue);
        report.add(newIssue2);

        IssuesStatistics compareIssuesStatistics = new IssuesStatisticsBuilder()
                .setTotalSize(3)
                .setTotalNormalSize(2)
                .setTotalErrorSize(1)
                .setNewSize(2)
                .setNewNormalSize(1)
                .setNewErrorSize(1)
                .setDeltaSize(1)
                .setDeltaErrorSize(1)
                .build();

        DeltaReport deltaReport = new DeltaReport(report, history, 0);
        IssuesStatistics issuesStatistics = deltaReport.getStatistics();
        IssuesStatisticsAssert.assertThat(issuesStatistics)
                .isNotNull()
                .isEqualToComparingFieldByField(compareIssuesStatistics);
    }

    private Issue getIssue(final String name) {
        return new IssueBuilder().setFileName(name).setFingerprint(name).build();
    }

    private Issue getIssueWithSeverity(final String name, final Severity severity) {
        return new IssueBuilder().setFileName(name).setFingerprint(name).setSeverity(severity).build();
    }
}
