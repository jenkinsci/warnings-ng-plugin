package io.jenkins.plugins.analysis.core.model;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import hudson.DescriptorExtensionList;
import hudson.model.ModelObject;
import hudson.model.Run;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.bootstrap5.MessagesViewModel;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DetailFactory}.
 *
 * @author Manuel Hampp
 */
class DetailFactoryTest {
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final Run<?, ?> RUN = mock(Run.class);
    private static final String[] ERROR_MESSAGES = {"error", "messages"};
    private static final String[] LOG_MESSAGES = {"log", "messages"};

    private static final Report NO_ISSUES = new Report();
    private static final Report ALL_ISSUES = createReportWith(3, 2, 1, "all");
    private static final Report NEW_ISSUES = createReportWith(3, 2, 1, "new");
    private static final Report OUTSTANDING_ISSUES = createReportWith(3, 2, 1, "outstanding");
    private static final Report FIXED_ISSUES = createReportWith(3, 2, 1, "fixed");
    private static final String PARENT_NAME = "Parent Name";
    private static final String AFFECTED_FILE_CONTENT = "Console-Log-Content";
    private static final String TOOL_ID = "spotbugs";

    @Test
    void shouldThrowExceptionIfLinkIsNotFound() {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() ->
                        new DetailFactory().createTrendDetails("broken", RUN, createResult(), ALL_ISSUES, NEW_ISSUES,
                                OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent()));
    }

    @Test
    void shouldCreateDetailsForEmpty() {
        var empty = new Report();

        var originDetails = createTrendDetails("origin.123", createResult(),
                empty, empty, empty, empty, createParent(),
                IssuesDetail.class);
        assertThat(originDetails).hasIssues(empty);
        assertThat(originDetails).hasFixedIssues(empty);
        assertThat(originDetails).hasNewIssues(empty);
        assertThat(originDetails).hasOutstandingIssues(empty);

        var fileDetails = createTrendDetails("file.123", createResult(),
                empty, empty, empty, empty, createParent(),
                IssuesDetail.class);
        assertThat(fileDetails).hasIssues(empty);
        assertThat(fileDetails).hasFixedIssues(empty);
        assertThat(fileDetails).hasNewIssues(empty);
        assertThat(fileDetails).hasOutstandingIssues(empty);
    }

    @Test
    void shouldCreateOrigin() {
        var result = createResult();
        Map<String, Integer> sizes = new HashMap<>();
        sizes.put(TOOL_ID, 20);
        when(result.getSizePerOrigin()).thenReturn(sizes);
        var details = createTrendDetails("origin." + TOOL_ID.hashCode(), result,
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);
        assertThat(details).hasIssues(ALL_ISSUES);
        var empty = createTrendDetails("origin.wrongID", result,
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);
        assertThat(empty.getIssues()).isEmpty();
    }

    @Test
    void shouldReturnFixedWarningsDetailWhenCalledWithFixedLink() {
        var details = createTrendDetails("fixed", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                FixedWarningsDetail.class);
        assertThat(details).hasIssues(FIXED_ISSUES);
        assertThat(details).hasFixedIssues(FIXED_ISSUES);
        assertThat(details).hasNewIssues(NO_ISSUES);
        assertThat(details).hasOutstandingIssues(NO_ISSUES);
    }

    @Test
    void shouldReturnAllIssues() {
        var details = createTrendDetails("all", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);
        assertThat(details).hasIssues(ALL_ISSUES);
        assertThat(details).hasFixedIssues(FIXED_ISSUES);
        assertThat(details).hasNewIssues(NEW_ISSUES);
        assertThat(details).hasOutstandingIssues(OUTSTANDING_ISSUES);
    }

    @Test
    void shouldReturnLabelProviderNameOnOrigin() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getDescriptorsFor(Tool.class)).thenReturn(
                DescriptorExtensionList.createDescriptorList((Jenkins) null, Tool.class));
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        var detailFactory = new DetailFactory(jenkins, buildFolder);
        var details = detailFactory.createTrendDetails("origin." + TOOL_ID.hashCode(), RUN,
                createResult(), ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());
        assertThat(details).isInstanceOfSatisfying(IssuesDetail.class,
                d -> assertThat(d.getDisplayName()).isEqualTo("Static Analysis"));
    }

    @Test
    void shouldReturnIssuesDetailWithNewIssuesWhenCalledWithNewLink() {
        var details = createTrendDetails("new", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);
        assertThat(details).hasIssues(NEW_ISSUES);
        assertThat(details).hasFixedIssues(NO_ISSUES);
        assertThat(details).hasNewIssues(NEW_ISSUES);
        assertThat(details).hasOutstandingIssues(NO_ISSUES);
    }

    @Test
    void shouldReturnIssuesDetailWithOutstandingIssuesWhenCalledWithOutstandingLink() {
        var details = createTrendDetails("outstanding", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);
        assertThat(details).hasIssues(OUTSTANDING_ISSUES);
        assertThat(details).hasFixedIssues(NO_ISSUES);
        assertThat(details).hasNewIssues(NO_ISSUES);
        assertThat(details).hasOutstandingIssues(OUTSTANDING_ISSUES);
    }

    @Test
    void shouldReturnPriorityDetailWithHighPriorityIssuesWhenCalledWithHighLink() {
        var details = createTrendDetails("HIGH", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);
        assertThatPrioritiesAreFiltered(details, Severity.WARNING_HIGH);
        assertThatPrioritiesAreCorrectlySet(details, 3, 0, 0);
    }

    @Test
    void shouldReturnPriorityDetailWithNormalPriorityIssuesWhenCalledWithNormalLink() {
        var details = createTrendDetails("NORMAL", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);

        assertThatPrioritiesAreFiltered(details, Severity.WARNING_NORMAL);
        assertThatPrioritiesAreCorrectlySet(details, 0, 2, 0);
    }

    @Test
    void shouldReturnPriorityDetailWithLowPriorityIssuesWhenCalledWithLowLink() {
        var details = createTrendDetails("LOW", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                IssuesDetail.class);

        assertThatPrioritiesAreFiltered(details, Severity.WARNING_LOW);
        assertThatPrioritiesAreCorrectlySet(details, 0, 0, 1);
    }

    private void assertThatPrioritiesAreFiltered(final IssuesDetail details, final Severity severity) {
        assertThat(details).hasIssues(ALL_ISSUES.filter(Issue.bySeverity(severity)));
        assertThat(details).hasFixedIssues(FIXED_ISSUES.filter(Issue.bySeverity(severity)));
        assertThat(details).hasNewIssues(NEW_ISSUES.filter(Issue.bySeverity(severity)));
        assertThat(details).hasOutstandingIssues(OUTSTANDING_ISSUES.filter(Issue.bySeverity(severity)));
    }

    @Test
    void shouldReturnInfoErrorDetailWhenCalledWithInfoLink() {
        var details = createTrendDetails("info", createResult(),
                ALL_ISSUES, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, createParent(),
                MessagesViewModel.class);
        assertThat(details.getErrorMessages()).containsExactly(ERROR_MESSAGES);
        assertThat(details.getDisplayName()).contains(PARENT_NAME);
    }

    @Test
    void shouldCreateConsoleDetailForSourceLinksIfFileNameIsSelf() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readConsoleLog(any())).thenReturn(createLines());
        var fileName = ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID;

        var details = createDetails(jenkins, buildFolder, fileName);
        assertThat(details).isInstanceOf(ConsoleDetail.class);
        assertThat(((ConsoleDetail) details).getSourceCode()).contains(AFFECTED_FILE_CONTENT);
    }

    private Object createDetails(final JenkinsFacade jenkins, final BuildFolderFacade buildFolder,
            final String fileName) {
        try (var issueBuilder = new IssueBuilder()) {
            var detailFactory = new DetailFactory(jenkins, buildFolder);

            issueBuilder.setFileName(fileName);
            var issue = issueBuilder.build();

            var report = new Report();
            report.add(issue);

            return detailFactory.createTrendDetails("source." + issue.getId().toString(),
                    RUN, createResult(), report, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING,
                    createParent());
        }
    }

    /**
     * Checks that the error message is shown if an affected file could not be read.
     */
    @Test
    void shouldShowExceptionMessageIfAffectedFileIsNotReadable() throws IOException {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readFile(any(), anyString(), any())).thenThrow(new IOException("file error"));

        var details = createDetails(jenkins, buildFolder, "a-file");

        assertThat(details).isInstanceOfSatisfying(SourceCodeViewModel.class,
                s -> assertThat(s.getSourceCode()).contains("IOException: file error"));
    }

    /**
     * Checks that a  to a source, returns a SourceDetail-View.
     */
    @Test
    void shouldReturnSourceDetailWhenCalledWithSourceLinkAndIssueNotInConsoleLog() throws IOException {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readFile(any(), anyString(), any())).thenReturn(new StringReader(AFFECTED_FILE_CONTENT));

        var details = createDetails(jenkins, buildFolder, "a-file");

        assertThat(details).isInstanceOfSatisfying(SourceCodeViewModel.class,
                s -> assertThat(s.getSourceCode()).contains(AFFECTED_FILE_CONTENT));
    }

    /**
     * Checks that a link with a filter, that results to an non empty set, returns an IssueDetail-View that only
     * contains filtered issues.
     */
    @Test
    void shouldReturnIssueDetailFiltered() {
        var detailFactory = new DetailFactory();
        AnalysisResult result = mock(AnalysisResult.class);

        var details = detailFactory.createTrendDetails("category." + "CATEGORY2".hashCode(), RUN, result, ALL_ISSUES,
                NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING, createParent());
        assertThat(details).isInstanceOf(IssuesDetail.class);

        var filtered = ((IssuesDetail) details).getIssues();
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0)).hasCategory("CATEGORY2").hasSeverity(Severity.WARNING_HIGH);
    }

    @SuppressWarnings("ParameterNumber")
    private <T extends ModelObject> T createTrendDetails(final String link,
            final AnalysisResult result,
            final Report allIssues, final Report newIssues,
            final Report outstandingIssues, final Report fixedIssues,
            final IssuesDetail parent, final Class<T> actualType) {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getDescriptorsFor(Tool.class)).thenReturn(DescriptorExtensionList.createDescriptorList((Jenkins) null, Tool.class));
        var detailFactory = new DetailFactory(jenkins, mock(BuildFolderFacade.class));
        var details = detailFactory.createTrendDetails(link, RUN,
                result, allIssues, newIssues, outstandingIssues, fixedIssues, ENCODING, parent);
        assertThat(details).isInstanceOf(actualType);
        return actualType.cast(details);
    }

    private Stream<String> createLines() {
        List<String> lines = new ArrayList<>();
        lines.add(AFFECTED_FILE_CONTENT);
        return lines.stream();
    }

    private void assertThatPrioritiesAreCorrectlySet(final IssuesDetail issuesDetail,
            final int expectedSizeHigh, final int expectedSizeNormal, final int expectedSizeLow) {
        assertThatReportHasSeverities(issuesDetail.getIssues(),
                0, expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
        assertThatReportHasSeverities(issuesDetail.getOutstandingIssues(),
                0, expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
        assertThatReportHasSeverities(issuesDetail.getFixedIssues(),
                0, expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
        assertThatReportHasSeverities(issuesDetail.getNewIssues(),
                0, expectedSizeHigh, expectedSizeNormal, expectedSizeLow);
    }

    private void assertThatReportHasSeverities(final Report report, final int expectedSizeError,
            final int expectedSizeHigh, final int expectedSizeNormal, final int expectedSizeLow) {
        assertThat(report.getSizeOf(Severity.ERROR)).isEqualTo(expectedSizeError);
        assertThat(report.getSizeOf(Severity.WARNING_HIGH)).isEqualTo(expectedSizeHigh);
        assertThat(report.getSizeOf(Severity.WARNING_NORMAL)).isEqualTo(expectedSizeNormal);
        assertThat(report.getSizeOf(Severity.WARNING_LOW)).isEqualTo(expectedSizeLow);
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
        when(labelProvider.getSmallIconUrl()).thenReturn(PARENT_NAME);
        when(labelProvider.getLargeIconUrl()).thenReturn(PARENT_NAME);

        when(parent.getLabelProvider()).thenReturn(labelProvider);

        return parent;
    }

    private static Report createReportWith(final int high, final int normal, final int low, final String link) {
        try (var builder = new IssueBuilder().setOrigin(TOOL_ID)) {
            var issues = new Report(TOOL_ID, "SpotBugs");
            for (int i = 0; i < high; i++) {
                issues.add(builder.setSeverity(Severity.WARNING_HIGH)
                        .setMessage(link + " - " + i)
                        .setCategory("CATEGORY" + i)
                        .build());
            }
            for (int i = 0; i < normal; i++) {
                issues.add(builder.setSeverity(Severity.WARNING_NORMAL)
                        .setMessage(link + " - " + i)
                        .setCategory("CATEGORY" + i)
                        .build());
            }
            for (int i = 0; i < low; i++) {
                issues.add(builder.setSeverity(Severity.WARNING_LOW)
                        .setMessage(link + " - " + i)
                        .setCategory("CATEGORY" + i)
                        .build());
            }
            return issues;
        }
    }
}
