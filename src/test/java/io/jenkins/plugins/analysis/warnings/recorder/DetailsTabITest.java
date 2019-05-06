package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class DetailsTabITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldPopulateDetailsTab() {
        FreeStyleProject project = createFreeStyleJobWithWarnings("javac");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_DISABLED, project,
                project.getLastBuild().getNumber() + "/java");
        assertThat(htmlPage).isNotNull();
        DetailsTab detailsTab = new DetailsTab(htmlPage);

        assertThat(detailsTab.getTabs()).hasSize(1);
    }

    private FreeStyleProject createFreeStyleJobWithWarnings(final String fileName) {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        copySingleFileToWorkspace(project, "../" + fileName + ".txt", "java.txt");
        return project;
    }
}