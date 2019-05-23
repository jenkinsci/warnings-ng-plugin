package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssuesTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the details-tab part of issue overview page.
 *
 * @author Nils Engelbrecht
 */
public class DetailsTabITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * When a single warning is being recognized only the issues-tab should be shown.
     */
    @Test
    public void shouldPopulateDetailsTabSingleWarning() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java1Warning.txt", "java.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        DetailsTab detailsTab = createDetailsTab(analysisResult);
        assertThat(detailsTab.getTabTypes()).containsOnlyOnce(TabType.ISSUES);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(TabType.ISSUES);

        IssuesTable active = detailsTab.getActive();
        assertThat(active.getRows()).hasSize(1);
    }

    /**
     * When two warnings are being recognized in one file the tabs issues, files and folders should be shown.
     */
    @Test
    public void shouldPopulateDetailsTabMultipleWarnings() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        DetailsTab detailsTab = createDetailsTab(analysisResult);
        assertThat(detailsTab.getTabTypes())
                .containsExactly(TabType.FOLDERS, TabType.FILES, TabType.ISSUES);

        assertThat(detailsTab.getActiveTabType()).isEqualTo(TabType.FOLDERS);

        PropertyTable folders = detailsTab.getActive();
        assertThat(folders.getRows()).hasSize(2);

        detailsTab.select(TabType.ISSUES);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(TabType.ISSUES);

        IssuesTable issues = detailsTab.getActive();
        assertThat(issues.getRows()).hasSize(2);

        detailsTab.select(TabType.FILES);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(TabType.FILES);

        PropertyTable files = detailsTab.getActive();
        assertThat(files.getRows()).hasSize(2);
    }

    /**
     * When switching details-tab and the page is being reloaded, the previously selected tab should be memorized and
     * still be active.
     */
    @Test
    public void shouldMemorizeSelectedTabAsActiveOnPageReload() {
        FreeStyleProject project = createFreeStyleJobWithWarnings();

        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        DetailsTab detailsTab = createDetailsTab(analysisResult);
        assertThat(detailsTab.getTabTypes())
                .containsExactly(TabType.FOLDERS, TabType.FILES, TabType.ISSUES);

        assertThat(detailsTab.getActiveTabType()).isEqualTo(TabType.FOLDERS);
        detailsTab.select(TabType.ISSUES);
        assertThat(detailsTab.getActiveTabType()).isEqualTo(TabType.ISSUES);

        DetailsTab refreshedTab = createDetailsTab(analysisResult);
        assertThat(refreshedTab.getActiveTabType()).isEqualTo(TabType.ISSUES);
    }

    private DetailsTab createDetailsTab(final AnalysisResult analysisResult) {
        return new DetailsTab(getWebPage(JavaScriptSupport.JS_ENABLED, analysisResult));
    }

    private FreeStyleProject createFreeStyleJobWithWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        return project;
    }
}