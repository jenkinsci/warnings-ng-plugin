package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.DetailsTabType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssuesTable;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the details-tab part of issue overview page.
 *
 * @author Nils Engelbrecht
 */
public class DetailsTabITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * When a single warning is being recognized only the issues-tab should be shown.
     */
    @Test
    public void shouldPopulateDetailsTabSingleWarning() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java1Warning.txt", "java.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_ENABLED, analysisResult);
        assertThat(htmlPage).isNotNull();

        DetailsTab detailsTab = new DetailsTab(htmlPage);
        assertThat(detailsTab.getTabTypes())
                .hasSize(1)
                .contains(DetailsTabType.ISSUES);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(DetailsTabType.ISSUES);
        assertThat(detailsTab.getActive()).isInstanceOf(IssuesTable.class);
    }

    /**
     * When two warnings are being recognized in one file the tabs issues, files and folders should be shown.
     */
    @Test
    public void shouldPopulateDetailsTab1File() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_ENABLED, analysisResult);
        assertThat(htmlPage).isNotNull();

        DetailsTab detailsTab = new DetailsTab(htmlPage);
        assertThat(detailsTab.getTabTypes())
                .hasSize(3)
                .contains(
                        DetailsTabType.ISSUES,
                        DetailsTabType.FILES,
                        DetailsTabType.FOLDERS);

        //TODO find inconsistency: singleRun of test vs all test with JenkinsPerSuite
        assertThat(detailsTab.getActiveTabType()).isEqualTo(DetailsTabType.FOLDERS);
        //assertThat(detailsTab.getActive()).isInstanceOf(FoldersTable.class);

        detailsTab.select(DetailsTabType.ISSUES);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(DetailsTabType.ISSUES);
        assertThat(detailsTab.getActive()).isInstanceOf(IssuesTable.class);
    }

    /**
     * The following test should test the functionality of the details-tab page-object when switching to a tab other
     * than the active one.
     */
    @Test
    public void shouldSwitchDetailsTabsCorrectly() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_ENABLED, analysisResult);
        assertThat(htmlPage).isNotNull();

        DetailsTab detailsTab = new DetailsTab(htmlPage);
        assertThat(detailsTab.getTabTypes())
                .hasSize(3)
                .contains(DetailsTabType.ISSUES,
                        DetailsTabType.FILES,
                        DetailsTabType.FOLDERS);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(DetailsTabType.FOLDERS);
        //assertThat(detailsTab.getActive()).isInstanceOf(FoldersTable.class);

        detailsTab.select(DetailsTabType.ISSUES);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(DetailsTabType.ISSUES);
        assertThat(detailsTab.getActive()).isInstanceOf(IssuesTable.class);
    }

    /**
     * When four warnings are being recognized in two files the tabs issues, files and folders should be shown.
     */
    @Test
    public void shouldPopulateDetailsTab2Files() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");
        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java2.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(JavaScriptSupport.JS_ENABLED, analysisResult);
        assertThat(htmlPage).isNotNull();

        DetailsTab detailsTab = new DetailsTab(htmlPage);
        assertThat(detailsTab.getTabTypes())
                .hasSize(3)
                .contains(
                        DetailsTabType.ISSUES,
                        DetailsTabType.FILES,
                        DetailsTabType.FOLDERS);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(DetailsTabType.FOLDERS);
        //assertThat(detailsTab.getActive()).isInstanceOf(FoldersTable.class);
    }

    private FreeStyleProject createFreeStyleJobWithWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        return project;
    }
}