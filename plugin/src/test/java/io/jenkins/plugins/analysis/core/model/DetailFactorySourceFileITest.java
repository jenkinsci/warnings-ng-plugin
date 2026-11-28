package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import io.jenkins.plugins.util.JenkinsFacade;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import org.eclipse.collections.impl.factory.Lists;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link DetailFactory} source file reading functionality.
 * These tests require a Jenkins instance because SourceCodeViewModel.create() checks permissions.
 *
 * @author Akash Manna
 */
class DetailFactorySourceFileITest extends IntegrationTestWithJenkinsPerSuite {
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final String AFFECTED_FILE_CONTENT = "public class Test { }";
    private static final Report NEW_ISSUES = new Report();
    private static final Report OUTSTANDING_ISSUES = new Report();
    private static final Report FIXED_ISSUES = new Report();

    /**
     * Checks that the error message is shown if an affected file could not be read.
     */
    @Test
    void shouldShowExceptionMessageIfAffectedFileIsNotReadable() throws IOException {
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readFile(any(), anyString(), any())).thenThrow(new IOException("file error"));

        var details = createDetails(buildFolder, "a-file");

        assertThat(details)
                .isInstanceOfSatisfying(
                        SourceCodeViewModel.class,
                        s -> assertThat(s.getSourceCode()).contains("IOException: file error"));
    }

    /**
     * Checks that a link to a source returns a SourceDetail-View.
     */
    @Test
    void shouldReturnSourceDetailWhenCalledWithSourceLinkAndIssueNotInConsoleLog() throws IOException {
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readFile(any(), anyString(), any())).thenReturn(new StringReader(AFFECTED_FILE_CONTENT));

        var details = createDetails(buildFolder, "a-file");

        assertThat(details)
                .isInstanceOfSatisfying(
                        SourceCodeViewModel.class,
                        s -> assertThat(s.getSourceCode()).contains(AFFECTED_FILE_CONTENT));
    }

    /**
     * Checks that the source code view shows the severity and the details of the issue as badges.
     */
    @Test
    void shouldShowSeverityAndBadgesOfIssue() throws IOException {
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readFile(any(), anyString(), any())).thenReturn(new StringReader(AFFECTED_FILE_CONTENT));

        try (var issueBuilder = new IssueBuilder()) {
            var issue = issueBuilder
                    .setFileName("a-file")
                    .setLineStart(1)
                    .setSeverity(Severity.WARNING_HIGH)
                    .setCategory("Design")
                    .setType("Coupling")
                    .setMessage("Message")
                    .build();
            var newIssues = new Report();
            newIssues.add(issue);

            var details = createDetails(buildFolder, issue, newIssues);

            assertThat(details).isInstanceOfSatisfying(SourceCodeViewModel.class, s -> {
                var marker = Jsoup.parse(s.getSourceCode()).getElementById("issue-" + issue.getId());
                assertThat(marker).isNotNull();
                assertThat(marker.classNames()).contains("prism-marker--error");
                assertThat(marker.attr("data-marker-state")).isEqualTo("new");
                assertThat(marker.attr("data-marker-tool")).isEqualTo("test");
                assertThat(marker.getElementsByClass("prism-marker__badge").eachText())
                        .contains("New", "Test", "Design · Coupling");
            });
        }
    }

    /**
     * Checks that issues in the console log are shown in an excerpt of the console log.
     */
    @Test
    void shouldShowExcerptOfConsoleLog() {
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.readConsoleLog(any()))
                .thenReturn(IntStream.rangeClosed(1, 50).mapToObj(line -> "line " + line));

        try (var issueBuilder = new IssueBuilder()) {
            var issue = issueBuilder
                    .setFileName(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID)
                    .setLineStart(20)
                    .setMessage("Message")
                    .build();

            var details = createDetails(buildFolder, issue, NEW_ISSUES);

            assertThat(details).isInstanceOfSatisfying(SourceCodeViewModel.class, s -> {
                assertThat(s.getDisplayName()).isEqualTo(Messages.ConsoleLog_Name());
                var document = Jsoup.parse(s.getSourceCode());
                var code = document.selectFirst(".prism-source__raw > code");
                assertThat(code).isNotNull();
                assertThat(code.attr("data-first-line")).isEqualTo("10");
                assertThat(code.attr("data-lines")).isEqualTo("21");
                assertThat(code.classNames()).isEmpty();
                assertThat(document.getElementById("issue-" + issue.getId())).isNotNull();
            });
        }
    }

    private Object createDetails(final BuildFolderFacade buildFolder, final String fileName) {
        try (var issueBuilder = new IssueBuilder()) {
            issueBuilder.setFileName(fileName);
            return createDetails(buildFolder, issueBuilder.build(), NEW_ISSUES);
        }
    }

    private Object createDetails(final BuildFolderFacade buildFolder, final Issue issue, final Report newIssues) {
        var detailFactory = new DetailFactory(new JenkinsFacade(), buildFolder);

        var report = new Report();
        report.add(issue);

        var project = createFreeStyleProject();
        buildSuccessfully(project);
        Run<?, ?> run = project.getLastBuild();

        return detailFactory.createTrendDetails(
                "source." + issue.getId().toString(),
                run,
                createAnalysisResult(),
                report,
                newIssues,
                OUTSTANDING_ISSUES,
                FIXED_ISSUES,
                ENCODING,
                createParent());
    }

    private AnalysisResult createAnalysisResult() {
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getErrorMessages()).thenReturn(Lists.immutable.empty());
        when(result.getInfoMessages()).thenReturn(Lists.immutable.empty());
        when(result.getBlames()).thenReturn(new Blames());
        return result;
    }

    private IssuesDetail createParent() {
        IssuesDetail parent = mock(IssuesDetail.class);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getId()).thenReturn("test");
        when(labelProvider.getName()).thenReturn("Test");
        when(labelProvider.getSmallIconUrl()).thenReturn("/icon");
        when(labelProvider.getLargeIconUrl()).thenReturn("/icon");
        when(parent.getLabelProvider()).thenReturn(labelProvider);
        return parent;
    }
}
