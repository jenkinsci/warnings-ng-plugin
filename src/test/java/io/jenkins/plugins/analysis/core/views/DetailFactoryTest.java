package io.jenkins.plugins.analysis.core.views;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

import hudson.model.ModelObject;
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
    private static final String AFFECTED_FILE_CONTENT = "Console-Log-Content";

    @SuppressWarnings("ParameterNumber")
    private <T extends ModelObject> T createTrendDetails(final String link, final Run<?, ?> owner, final AnalysisResult result,
            final Report allIssues, final Report newIssues,
            final Report outstandingIssues, final Report fixedIssues,
            final Charset sourceEncoding, final IssuesDetail parent, final Class<T> actualType) {
        DetailFactory detailFactory = new DetailFactory();
        Object details = detailFactory.createTrendDetails(link, owner, 
                result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(details).isInstanceOf(actualType);
        return actualType.cast(details);
    }
     
    @Test
    void shouldReturnFixedWarningsDetailWhenCalledWithFixedLink() {
        FixedWarningsDetail details = createTrendDetails("fixed", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent(),
                FixedWarningsDetail.class);
        assertThat(details).hasIssues(FIXED_ISSUES);
    }

    @Test
    void shouldReturnIssuesDetailWithNewIssuesWhenCalledWithNewLink() {
        IssuesDetail details = createTrendDetails("new", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent(),
                IssuesDetail.class);
        assertThat(details).hasIssues(NEW_ISSUES);
    }

    @Test
    void shouldReturnIssuesDetailWithOutstandingIssuesWhenCalledWithOutstandingLink() {
        IssuesDetail details = createTrendDetails("outstanding", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent(), 
                IssuesDetail.class);
        assertThat(details).hasIssues(OUTSTANDING_ISSUES);
    }

    @Test
    void shouldReturnPriorityDetailWithHighPriorityIssuesWhenCalledWithHighLink() {
        IssuesDetail details = createTrendDetails("HIGH", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent(), 
                IssuesDetail.class);
        assertThat(details).hasIssues(ALL_ISSUES.filter(Issue.bySeverity(Severity.WARNING_HIGH)));
        assertThatPrioritiesAreCorrectlySet(details, 3, 0, 0);
    }

    @Test
    void shouldReturnPriorityDetailWithNormalPriorityIssuesWhenCalledWithNormalLink() {
        IssuesDetail details = createTrendDetails("NORMAL", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent(), 
                IssuesDetail.class);

        assertThat(details).hasIssues(ALL_ISSUES.filter(Issue.bySeverity(Severity.WARNING_NORMAL)));
        assertThatPrioritiesAreCorrectlySet(details, 0, 2, 0);
    }

    @Test
    void shouldReturnPriorityDetailWithLowPriorityIssuesWhenCalledWithLowLink() {
        IssuesDetail details = createTrendDetails("LOW", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent(), 
                IssuesDetail.class);

        assertThat(details).hasIssues(ALL_ISSUES.filter(Issue.bySeverity(Severity.WARNING_LOW)));
        assertThatPrioritiesAreCorrectlySet(details, 0, 0, 1);
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
        InfoErrorDetail details = createTrendDetails("info", RUN, createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent(), InfoErrorDetail.class);
        assertThat(details.getErrorMessages()).containsExactly(ERROR_MESSAGES);
        assertThat(details.getDisplayName()).contains(PARENT_NAME);
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

    @Test
    void shouldCreateConsoleDetailForSourceLinksIfFileNameIsSelf() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.readConsoleLog(any())).thenReturn(createLines());
        DetailFactory detailFactory = new DetailFactory(jenkins);
        Report report = new Report();

        IssueBuilder issueBuilder = new IssueBuilder();
        issueBuilder.setFileName(IssueParser.SELF);
        Issue issue = issueBuilder.build();

        report.add(issue);
        
        Object details = detailFactory.createTrendDetails("source." + issue.getId().toString(), 
                RUN, createResult(), report, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());
        assertThat(details).isInstanceOf(ConsoleDetail.class);
        assertThat(((ConsoleDetail) details).getSourceCode()).contains(AFFECTED_FILE_CONTENT);
    }

    private Stream<String> createLines() {
        List<String> lines = new ArrayList<>();
        lines.add(AFFECTED_FILE_CONTENT);
        return lines.stream();
    }

    /**
     * Checks that a link to a source, returns a SourceDetail-View.
     */
    @Test
    void shouldReturnSourceDetailWhenCalledWithSourceLinkAndIssueNotInConsoleLog() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.readBuildFile(any(), anyString(), any())).thenReturn(new StringReader(AFFECTED_FILE_CONTENT));
        
        DetailFactory detailFactory = new DetailFactory(jenkins);
        Report report = new Report();

        IssueBuilder issueBuilder = new IssueBuilder();
        issueBuilder.setFileName("a-file");
        Issue issue = issueBuilder.build();

        report.add(issue);

        Object details = detailFactory.createTrendDetails("source." + issue.getId().toString(),
                RUN, createResult(), report, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());
        assertThat(details).isInstanceOf(SourceDetail.class);
        assertThat(((SourceDetail) details).getSourceCode()).contains(AFFECTED_FILE_CONTENT);
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