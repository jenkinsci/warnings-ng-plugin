package io.jenkins.plugins.analysis.core.views;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.model.Run;

/**
 * Tests the class {@link DetailFactory}.
 *
 * @author Manuel Hampp
 */
class DetailFactoryTest {
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final Run RUN = mock(Run.class);
    private static final String[] ERROR_MESSAGES = new String[]{"error", "messages"};
    private static final String[] LOG_MESSAGES = new String[]{"log", "messages"};

    private static final Report ALL_ISSUES = createReportWith(3, 2, 1, "all");
    private static final Report NEW_ISSUES = createReportWith(3, 2, 1, "new");
    private static final Report OUTSTANDING_ISSUES = createReportWith(3, 2, 1, "outstanding");
    private static final Report FIXED_ISSUES = createReportWith(3, 2, 1, "fixed");
    private static final String PARENT_NAME = "Parent Name";

    @Test
    void shouldReturnFixedWarningsDetailWhenCalledWithFixedLink() {
        DetailFactory detailFactory = new DetailFactory();

        Object fixedWarningsDetail = detailFactory.createTrendDetails("fixed", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());

        assertThat(fixedWarningsDetail).isInstanceOf(FixedWarningsDetail.class);
        assertThat((FixedWarningsDetail) fixedWarningsDetail).hasIssues(FIXED_ISSUES);
    }

    @Test
    void shouldReturnIssuesDetailWithNewIssuesWhenCalledWithNewLink() {
        DetailFactory detailFactory = new DetailFactory();

        Object issuesDetail = detailFactory.createTrendDetails("new", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());

        assertThat(issuesDetail).isInstanceOf(IssuesDetail.class);
        assertThat((IssuesDetail) issuesDetail).hasIssues(NEW_ISSUES);
    }

    @Test
    void shouldReturnIssuesDetailWithOutstandingIssuesWhenCalledWithOutstandingLink() {
        DetailFactory detailFactory = new DetailFactory();

        Object issuesDetail = detailFactory.createTrendDetails("outstanding", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());

        assertThat(issuesDetail).isInstanceOf(IssuesDetail.class);
        assertThat((IssuesDetail) issuesDetail).hasIssues(OUTSTANDING_ISSUES);
    }

    @Test
    void shouldReturnPriorityDetailWithHighPriorityIssuesWhenCalledWithHighLink() {
        DetailFactory detailFactory = new DetailFactory();

        Object detail = detailFactory.createTrendDetails("HIGH", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());

        assertThat(detail).isInstanceOf(IssuesDetail.class);
        IssuesDetail issuesDetail = (IssuesDetail) detail;
        assertThat(issuesDetail).hasIssues(ALL_ISSUES.filter(Issue.bySeverity(Severity.WARNING_HIGH)));

        assertThatPrioritiesAreCorrectlySet(issuesDetail, 3, 0, 0);
    }

    @Test
    void shouldReturnPriorityDetailWithNormalPriorityIssuesWhenCalledWithNormalLink() {
        DetailFactory detailFactory = new DetailFactory();

        Object detail = detailFactory.createTrendDetails("NORMAL", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());

        assertThat(detail).isInstanceOf(IssuesDetail.class);
        IssuesDetail issuesDetail = (IssuesDetail) detail;
        assertThat(issuesDetail).hasIssues(ALL_ISSUES.filter(Issue.bySeverity(Severity.WARNING_NORMAL)));

        assertThatPrioritiesAreCorrectlySet(issuesDetail, 0, 2, 0);
    }

    @Test
    void shouldReturnPriorityDetailWithLowPriorityIssuesWhenCalledWithLowLink() {
        DetailFactory detailFactory = new DetailFactory();

        Object detail = detailFactory.createTrendDetails("LOW", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());

        assertThat(detail).isInstanceOf(IssuesDetail.class);
        IssuesDetail issuesDetail = (IssuesDetail) detail;
        assertThat(issuesDetail).hasIssues(ALL_ISSUES.filter(Issue.bySeverity(Severity.WARNING_LOW)));

        assertThatPrioritiesAreCorrectlySet(issuesDetail, 0, 0, 1);
    }

    private void assertThatPrioritiesAreCorrectlySet(final IssuesDetail issuesDetail,
            final int expectedSizeHigh, final int expectedSizeNormal, final int expectedSizeLow) {
        assertThat(issuesDetail.getIssues()).hasPriorities(expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
        assertThat(issuesDetail.getOutstandingIssues()).hasPriorities(expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
        assertThat(issuesDetail.getFixedIssues()).hasPriorities(expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
        assertThat(issuesDetail.getNewIssues()).hasPriorities(expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
    }

    @Test
    void shouldReturnInfoErrorDetailWhenCalledWithInfoLink() {
        DetailFactory detailFactory = new DetailFactory();

        Object detail = detailFactory.createTrendDetails("info", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());

        assertThat(detail).isInstanceOf(InfoErrorDetail.class);
        InfoErrorDetail errorDetail = (InfoErrorDetail) detail;
        assertThat(errorDetail.getErrorMessages()).containsExactly(ERROR_MESSAGES);
        assertThat(errorDetail.getDisplayName()).contains(PARENT_NAME);
    }

    @Test
    void shouldReturnParentIfIssuesAreEmpty() {
        DetailFactory detailFactory = new DetailFactory();

        IssuesDetail parent = createParent();
        Report empty = new Report();
        Object issuesDetail = detailFactory.createTrendDetails("foo.bar", RUN, createResult(),
                empty, empty, empty, empty, ENCODING, parent);

        assertThat(issuesDetail).isSameAs(parent);
    }

    /**
     * Checks that a link to a source, returns a ConsoleDetail-View if the issue is contained in the console log.
     */
    @Test
    void shouldReturnConsoleDetailWhenCalledWithSourceLinkAndIssueInConsoleLog() throws IOException {
        DetailFactory detailFactory = new DetailFactory();
        @SuppressWarnings("unchecked")
        Report allIssuesWithUUIDIssue = mock(Report.class);
        Issue issueFromUUID = mock(Issue.class);
        File file = File.createTempFile("test", "file");

        when(allIssuesWithUUIDIssue.findById(any())).thenReturn(issueFromUUID);
        when(issueFromUUID.getFileName()).thenReturn("<SELF>");
        when(RUN.getLogFile()).thenReturn(file);

        Object consoleDetail = detailFactory.createTrendDetails("source." + UUID.randomUUID().toString(), RUN, createResult(),
                allIssuesWithUUIDIssue,
                NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());
        assertThat(consoleDetail).isInstanceOf(ConsoleDetail.class);
    }

    /**
     * Checks that a link to a source, returns a SourceDetail-View.
     */
    @Test
    void shouldReturnSourceDetailWhenCalledWithSourceLinkAndIssueNotInConsoleLog() throws IOException {
        DetailFactory detailFactory = new DetailFactory();
        @SuppressWarnings("unchecked")
        Report allIssuesWithUUIDIssue = mock(Report.class);
        Job parentJob = mock(Job.class);
        File file = File.createTempFile("test", "file");
        Issue issueFromUUID = mock(Issue.class);

        when(allIssuesWithUUIDIssue.findById(any())).thenReturn(issueFromUUID);
        when(RUN.getParent()).thenReturn(parentJob);
        when(parentJob.getBuildDir()).thenReturn(file);
        when(issueFromUUID.getFileName()).thenReturn("test");

        Object fixedWarningsDetail = detailFactory.createTrendDetails("source." + UUID.randomUUID().toString(), RUN,
                createResult(), allIssuesWithUUIDIssue,
                NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());
        assertThat(fixedWarningsDetail).isInstanceOf(SourceDetail.class);
    }

    /**
     * Checks that a link with a filter, that results to an non empty set, returns an IssueDetail-View that only
     * contains filtered issues.
     */
    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnIssueDetailFiltered() {
        DetailFactory detailFactory = new DetailFactory();
        AnalysisResult result = mock(AnalysisResult.class);
        Report allIssuesFilterable = mock(Report.class);
        Report newIssuesFilterable = mock(Report.class);
        Report outstandingIssuesFilterable = mock(Report.class);
        Report fixedIssuesFilterable = mock(Report.class);
        Report filteredIssues = mock(Report.class);
        Issue filteredIssuesOnZeroPosition = mock(Issue.class);

        when(allIssuesFilterable.filter(any())).thenReturn(filteredIssues);
        when(newIssuesFilterable.filter(any())).thenReturn(filteredIssues);
        when(outstandingIssuesFilterable.filter(any())).thenReturn(filteredIssues);
        when(fixedIssuesFilterable.filter(any())).thenReturn(filteredIssues);
        when(filteredIssues.isEmpty()).thenReturn(false);
        when(filteredIssues.get(0)).thenReturn(filteredIssuesOnZeroPosition);

        Object issueDetail = detailFactory.createTrendDetails("foo.bar", RUN, result, allIssuesFilterable,
                newIssuesFilterable, outstandingIssuesFilterable, fixedIssuesFilterable, ENCODING, createParent());
        IssuesDetail issuesDetailCasted = (IssuesDetail) issueDetail;

        assertThat(issueDetail).isInstanceOf(IssuesDetail.class);
        assertThat(issuesDetailCasted.getNewIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
        assertThat(issuesDetailCasted.getOutstandingIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
        assertThat(issuesDetailCasted.getIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
        assertThat(issuesDetailCasted.getFixedIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
    }

    private AnalysisResult createResult() {
        AnalysisResult result = mock(AnalysisResult.class);
        
        when(result.getErrorMessages()).thenReturn(Lists.immutable.of(ERROR_MESSAGES));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.of(LOG_MESSAGES));
        
        return result;
    }

    private IssuesDetail createParent() {
        IssuesDetail parent = mock(IssuesDetail.class);

        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getName()).thenReturn(PARENT_NAME);

        when(parent.getLabelProvider()).thenReturn(labelProvider);
        
        return parent;
    }

    private static Report createReportWith(final int high, final int normal, final int low, final String link) {
        IssueBuilder builder = new IssueBuilder();
        Report issues = new Report();
        for (int i = 0; i < high; i++) {
            issues.add(builder.setPriority(Priority.HIGH).setMessage(link + " - " + i).build());
        }
        for (int i = 0; i < normal; i++) {
            issues.add(builder.setPriority(Priority.NORMAL).setMessage(link + " - " + i).build());
        }
        for (int i = 0; i < low; i++) {
            issues.add(builder.setPriority(Priority.LOW).setMessage(link + " - " + i).build());
        }
        return issues;
    }
}