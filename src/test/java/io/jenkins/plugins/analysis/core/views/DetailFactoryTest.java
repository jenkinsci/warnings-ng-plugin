package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the class {@link DetailFactory}.
 *
 * @author Manuel Hampp
 */
class DetailFactoryTest {

    /**
     * Checks that a link to the fixed view, returns a FixedWarningsDetail-View
     */
    @Test
    void shouldReturnFixedWarningsDetailWhenCallWithFixedLink() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        Issues<?> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        Object fixedWarningsDetail = detailFactory.createTrendDetails("fixed", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);

        assertThat(fixedWarningsDetail).isInstanceOf(FixedWarningsDetail.class);
    }

    /**
     * Checks that a link to the new view, returns a IssueDetail-View
     */
    @Test
    void shouldReturnIssuesDetailWhenCallWithNewLink() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        Issues<?> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        Object issuesDetail = detailFactory.createTrendDetails("new", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);

        assertThat(issuesDetail).isInstanceOf(IssuesDetail.class);
    }

    /**
     * Checks that a link to the outstanding view, returns a IssueDetail-View
     */
    @Test
    void shouldReturnIssuesDetailWhenCallWithOutstandingLink() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        Issues<?> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        Object issuesDetail = detailFactory.createTrendDetails("outstanding", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);

        assertThat(issuesDetail).isInstanceOf(IssuesDetail.class);
    }

    /**
     * Checks that a link to the info view, returns a InfoErrorDetail-View
     */
    @Test
    void shouldReturnInfoErrorDetailWhenCallWithInfoLink() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        List<String> valueList = Arrays.asList("foo", "bar");
        Issues<?> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        when(result.getErrorMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(parent.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider("labelprovider"));

        Object infoErrorDetail = detailFactory.createTrendDetails("info", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(infoErrorDetail).isInstanceOf(InfoErrorDetail.class);
    }

    /**
     * Checks that a link to a source, returns a ConsoleDetail-View if the issue is contained in the console log.
     */
    @Test
    void shouldReturnConsoleDetailWhenCallWithSourceLinkAndIssueInConsoleLog() throws IOException {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);
        Issue issueFromUUID = mock(Issue.class);
        File file = File.createTempFile("test", "file");

        when(allIssues.findById(any())).thenReturn(issueFromUUID);
        when(issueFromUUID.getFileName()).thenReturn("<SELF>");
        when(owner.getLogFile()).thenReturn(file);

        Object consoleDetail = detailFactory.createTrendDetails("source." + UUID.randomUUID().toString(), owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(consoleDetail).isInstanceOf(ConsoleDetail.class);
    }

    /**
     * Checks that a link to a source, returns a SourceDetail-View
     */
    @Test
    void shouldReturnSourceDetailWhenCallWithSourceLinkAndIssueNotInConsoleLog() throws IOException {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);
        Job parentJob = mock(Job.class);
        File file = File.createTempFile("test", "file");
        Issue issueFromUUID = mock(Issue.class);

        when(allIssues.findById(any())).thenReturn(issueFromUUID);
        when(owner.getParent()).thenAnswer(invocation -> parentJob);
        when(parentJob.getBuildDir()).thenReturn(file);
        when(issueFromUUID.getFileName()).thenReturn("test");

        Object fixedWarningsDetail = detailFactory.createTrendDetails("source." + UUID.randomUUID().toString(), owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(fixedWarningsDetail).isInstanceOf(SourceDetail.class);
    }

    /**
     * Checks that a link to high priority, returns a IssueDetail-View
     */
    @Test
    void shouldReturnPriorityDetailWhenCallWithSourceLinkHIGH() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        List<String> valueList = Arrays.asList("foo", "bar");
        @SuppressWarnings("unchecked")
        Issues<Issue> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        when(parent.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider("labelprovider"));
        when(result.getErrorMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.ofAll(valueList));

        Object issueDetail = detailFactory.createTrendDetails("HIGH", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(issueDetail).isInstanceOf(IssuesDetail.class);
    }

    /**
     * Checks that a link to normal priority view, returns a IssueDetail-View
     */
    @Test
    void shouldReturnPriorityDetailWhenCallWithSourceLinkNORMAL() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        List<String> valueList = Arrays.asList("foo", "bar");
        @SuppressWarnings("unchecked")
        Issues<Issue> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        when(parent.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider("labelprovider"));
        when(result.getErrorMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.ofAll(valueList));

        Object issueDetail = detailFactory.createTrendDetails("NORMAL", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(issueDetail).isInstanceOf(IssuesDetail.class);
    }

    /**
     * Checks that a link to low priority view, returns a IssueDetail-View
     */
    @Test
    void shouldReturnPriorityDetailWhenCallWithSourceLinkLOW() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        List<String> valueList = Arrays.asList("foo", "bar");
        @SuppressWarnings("unchecked")
        Issues<Issue> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        when(parent.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider("labelprovider"));
        when(result.getErrorMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.ofAll(valueList));

        Object issueDetail = detailFactory.createTrendDetails("LOW", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(issueDetail).isInstanceOf(IssuesDetail.class);
    }

    /**
     * Checks that a link to high priority view, returns a IssueDetail that only contains high priority issues.
     */
    @Test
    void shouldReturnPriorityDetailWithOnlyHighPriorityIssues() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);

        List<String> valueList = Arrays.asList("foo", "bar");

        Issues<?> allIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> newIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> outstandingIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> fixedIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        when(parent.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider("labelprovider"));
        when(result.getErrorMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.ofAll(valueList));

        IssuesDetail issueDetail = (IssuesDetail) detailFactory.createTrendDetails("HIGH", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);

        assertThat(issueDetail.getFixedIssues().getHighPrioritySize()).isEqualTo(3);
        assertThat(issueDetail.getFixedIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getFixedIssues().getLowPrioritySize()).isEqualTo(0);

        assertThat(issueDetail.getIssues().getHighPrioritySize()).isEqualTo(3);
        assertThat(issueDetail.getIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getIssues().getLowPrioritySize()).isEqualTo(0);

        assertThat(issueDetail.getOutstandingIssues().getHighPrioritySize()).isEqualTo(3);
        assertThat(issueDetail.getOutstandingIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getOutstandingIssues().getLowPrioritySize()).isEqualTo(0);

        assertThat(issueDetail.getNewIssues().getHighPrioritySize()).isEqualTo(3);
        assertThat(issueDetail.getNewIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getNewIssues().getLowPrioritySize()).isEqualTo(0);
    }

    /**
     * Checks that a link to normal priority view, returns a IssueDetail that only contains normal priority issues.
     */
    @Test
    void shouldReturnPriorityDetailWithOnlyNormalPriorityIssues() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);

        List<String> valueList = Arrays.asList("foo", "bar");

        Issues<?> allIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> newIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> outstandingIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> fixedIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        when(parent.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider("labelprovider"));
        when(result.getErrorMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.ofAll(valueList));

        IssuesDetail issueDetail = (IssuesDetail) detailFactory.createTrendDetails("NORMAL", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);

        assertThat(issueDetail.getFixedIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getFixedIssues().getNormalPrioritySize()).isEqualTo(2);
        assertThat(issueDetail.getFixedIssues().getLowPrioritySize()).isEqualTo(0);

        assertThat(issueDetail.getIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getIssues().getNormalPrioritySize()).isEqualTo(2);
        assertThat(issueDetail.getIssues().getLowPrioritySize()).isEqualTo(0);

        assertThat(issueDetail.getOutstandingIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getOutstandingIssues().getNormalPrioritySize()).isEqualTo(2);
        assertThat(issueDetail.getOutstandingIssues().getLowPrioritySize()).isEqualTo(0);

        assertThat(issueDetail.getNewIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getNewIssues().getNormalPrioritySize()).isEqualTo(2);
        assertThat(issueDetail.getNewIssues().getLowPrioritySize()).isEqualTo(0);
    }

    /**
     * Checks that a link to low priority view, returns a IssueDetail that only contains low priority issues.
     */
    @Test
    void shouldReturnPriorityDetailWithOnlyLowPriorityIssues() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);

        List<String> valueList = Arrays.asList("foo", "bar");

        Issues<?> allIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> newIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> outstandingIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Issues<?> fixedIssues = issueWithPriorityDeliveryService(3, 2, 1);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);

        when(parent.getLabelProvider()).thenReturn(new StaticAnalysisLabelProvider("labelprovider"));
        when(result.getErrorMessages()).thenReturn(Lists.immutable.ofAll(valueList));
        when(result.getInfoMessages()).thenReturn(Lists.immutable.ofAll(valueList));

        IssuesDetail issueDetail = (IssuesDetail) detailFactory.createTrendDetails("LOW", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);

        assertThat(issueDetail.getFixedIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getFixedIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getFixedIssues().getLowPrioritySize()).isEqualTo(1);

        assertThat(issueDetail.getIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getIssues().getLowPrioritySize()).isEqualTo(1);

        assertThat(issueDetail.getOutstandingIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getOutstandingIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getOutstandingIssues().getLowPrioritySize()).isEqualTo(1);

        assertThat(issueDetail.getNewIssues().getHighPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getNewIssues().getNormalPrioritySize()).isEqualTo(0);
        assertThat(issueDetail.getNewIssues().getLowPrioritySize()).isEqualTo(1);
    }

    /**
     * Checks that a link with a filter, that results to an empty set, returns the parent view.
     */
    @Test
    void shouldReturnParentIssueDetail() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> allIssues = mock(Issues.class);
        Issues<?> newIssues = mock(Issues.class);
        Issues<?> outstandingIssues = mock(Issues.class);
        Issues<?> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> emptyIssues = mock(Issues.class);

        when(allIssues.filter(any())).thenReturn(emptyIssues);
        when(emptyIssues.isEmpty()).thenReturn(true);

        Object issueDetail = detailFactory.createTrendDetails("foo.bar", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        assertThat(issueDetail).isEqualTo(parent);
    }

    /**
     * Checks that a link with a filter, that results to an non empty set, returns an IssueDetail-View that only contains filtered issues.
     */
    @Test
    void shouldReturnIssueDetailFiltered() {
        DetailFactory detailFactory = new DetailFactory();
        Run<?, ?> owner = mock(Run.class);
        AnalysisResult result = mock(AnalysisResult.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> allIssues = mock(Issues.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> newIssues = mock(Issues.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> outstandingIssues = mock(Issues.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> fixedIssues = mock(Issues.class);
        Charset sourceEncoding = Charset.defaultCharset();
        IssuesDetail parent = mock(IssuesDetail.class);
        @SuppressWarnings("unchecked")
        Issues<Issue> filteredIssues = mock(Issues.class);
        Issue filteredIssuesOnZeroPosition = mock(Issue.class);

        when(allIssues.filter(any())).thenReturn(filteredIssues);
        when(newIssues.filter(any())).thenReturn(filteredIssues);
        when(outstandingIssues.filter(any())).thenReturn(filteredIssues);
        when(fixedIssues.filter(any())).thenReturn(filteredIssues);
        when(filteredIssues.isEmpty()).thenReturn(false);
        when(filteredIssues.get(0)).thenReturn(filteredIssuesOnZeroPosition);

        Object issueDetail = detailFactory.createTrendDetails("foo.bar", owner, result, allIssues, newIssues, outstandingIssues, fixedIssues, sourceEncoding, parent);
        IssuesDetail issuesDetailCasted = (IssuesDetail) issueDetail;

        assertThat(issueDetail).isInstanceOf(IssuesDetail.class);
        assertThat(issuesDetailCasted.getNewIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
        assertThat(issuesDetailCasted.getOutstandingIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
        assertThat(issuesDetailCasted.getIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
        assertThat(issuesDetailCasted.getFixedIssues().get(0)).isEqualTo(filteredIssuesOnZeroPosition);
    }

    private Issues<?> issueWithPriorityDeliveryService(int prioHigh, int prioNormal, int prioLow) {
        Issues<Issue> issues = new Issues<>();
        for (int i = 0; i < prioHigh; i++) {
            Issue issue = mock(Issue.class);
            when(issue.getPriority()).thenReturn(Priority.HIGH);
            issues.add(issue);
        }
        for (int i = 0; i < prioNormal; i++) {
            Issue issue = mock(Issue.class);
            when(issue.getPriority()).thenReturn(Priority.NORMAL);
            issues.add(issue);
        }
        for (int i = 0; i < prioLow; i++) {
            Issue issue = mock(Issue.class);
            when(issue.getPriority()).thenReturn(Priority.LOW);
            issues.add(issue);
        }
        return issues;
    }
}