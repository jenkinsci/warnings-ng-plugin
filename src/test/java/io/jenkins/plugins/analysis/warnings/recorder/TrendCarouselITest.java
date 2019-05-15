package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsViewTrendCarousel;

/**
 * Provides tests for the trend carousel shown on the details page.
 */
public class TrendCarouselITest extends IntegrationTestWithJenkinsPerSuite {


    /**
     * Test that tools trend chart is default.
     */
    @Test
    public void shouldShowToolsTrendChart() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        List<AnalysisResult> buildResults = new ArrayList<>();
        createWorkspaceFileWithWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        createWorkspaceFileWithWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        createWorkspaceFileWithWarnings(project, 3);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewTrendCarousel carousel = new DetailsViewTrendCarousel(
                getDetailsWebPage(project, buildResults.get(0)));
        carousel.clickCarouselControlNext();
        carousel.clickCarouselControlNext();
        carousel.clickCarouselControlNext();
    }


    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    }

    private void createWorkspaceFileWithWarnings(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createDeprecationWarning(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac.txt", warningText.toString());
    }

    private String createDeprecationWarning(final int lineNumber) {
        return String.format(
                "[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n",
                lineNumber);
    }
}
