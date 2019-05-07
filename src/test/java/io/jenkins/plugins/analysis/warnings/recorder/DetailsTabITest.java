package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the details-tab part of issue overview page.
 *
 * @author Nils Engelbrecht
 */
public class DetailsTabITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldPopulateDetailsTabSingleWarning() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java1Warning.txt", "java.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_ENABLED, project,
                project.getLastBuild().getNumber() + "/java");
        assertThat(htmlPage).isNotNull();
        DetailsTab detailsTab = new DetailsTab(htmlPage);
        assertThat(detailsTab.getTabs()).hasSize(1);
        assertThat(detailsTab.getTabs().containsKey("Issues")).isTrue();
    }

    @Test
    public void shouldPopulateDetailsTab1File() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_ENABLED, project,
                project.getLastBuild().getNumber() + "/java");
        assertThat(htmlPage).isNotNull();
        DetailsTab detailsTab = new DetailsTab(htmlPage);
        assertThat(detailsTab.getTabs()).hasSize(3);
        assertThat(detailsTab.getTabs().containsKey("Issues")).isTrue();
        assertThat(detailsTab.getTabs().containsKey("Folders")).isTrue();
        assertThat(detailsTab.getTabs().containsKey("Files")).isTrue();
    }

    @Test
    public void shouldPopulateDetailsTab2Files() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");
        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java2.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_ENABLED, project,
                project.getLastBuild().getNumber() + "/java");
        assertThat(htmlPage).isNotNull();
        DetailsTab detailsTab = new DetailsTab(htmlPage);
        assertThat(detailsTab.getTabs()).hasSize(3);
        assertThat(detailsTab.getTabs().containsKey("Issues")).isTrue();
        assertThat(detailsTab.getTabs().containsKey("Folders")).isTrue();
        assertThat(detailsTab.getTabs().containsKey("Files")).isTrue();
    }

    private FreeStyleProject createFreeStyleJobWithWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        return project;
    }
}