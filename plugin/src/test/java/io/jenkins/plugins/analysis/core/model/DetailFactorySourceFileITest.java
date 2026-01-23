package io.jenkins.plugins.analysis.core.model;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        assertThat(details).isInstanceOfSatisfying(SourceCodeViewModel.class,
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

        assertThat(details).isInstanceOfSatisfying(SourceCodeViewModel.class,
                s -> assertThat(s.getSourceCode()).contains(AFFECTED_FILE_CONTENT));
    }

    private Object createDetails(final BuildFolderFacade buildFolder,
            final String fileName) {
        try (var issueBuilder = new IssueBuilder()) {
            var detailFactory = new DetailFactory(new JenkinsFacade(), buildFolder);

            issueBuilder.setFileName(fileName);
            var issue = issueBuilder.build();

            var report = new Report();
            report.add(issue);

            var project = createFreeStyleProject();
            buildSuccessfully(project);
            Run<?, ?> run = project.getLastBuild();

            return detailFactory.createTrendDetails("source." + issue.getId().toString(),
                    run, createAnalysisResult(), report, NEW_ISSUES, OUTSTANDING_ISSUES, FIXED_ISSUES, ENCODING,
                    createParent());
        }
    }

    private AnalysisResult createAnalysisResult() {
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getErrorMessages()).thenReturn(Lists.immutable.empty());
        when(result.getInfoMessages()).thenReturn(Lists.immutable.empty());
        return result;
    }

    private IssuesDetail createParent() {
        IssuesDetail parent = mock(IssuesDetail.class);
        StaticAnalysisLabelProvider labelProvider = mock(StaticAnalysisLabelProvider.class);
        when(labelProvider.getName()).thenReturn("Test");
        when(labelProvider.getSmallIconUrl()).thenReturn("/icon");
        when(labelProvider.getLargeIconUrl()).thenReturn("/icon");
        when(parent.getLabelProvider()).thenReturn(labelProvider);
        return parent;
    }
}
