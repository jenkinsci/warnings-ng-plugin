package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.jenkinsci.plugins.flexible_publish.JobUpdater.*;

public class FlexiblePublishITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JAVA_FILE = "java1Warning.txt";

    @Test
    public void shouldAnalyseJavaTwice() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, JAVA_FILE, JAVA_FILE);

        Java java = new Java();
        java.setPattern(JAVA_FILE);
        enableWarnings(project, java);

        //def job = hudson.model.Hudson.instance.getItem('My Job')
        //list job

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getTotalSize()).isEqualTo(1);
        //assertThat(result.getInfoMessages().contains(HEALTH_REPORT_ENABLED_MESSAGE));

        //InfoPage infoPage = new InfoPage(project, 1);
        //assertThat(result.getInfoMessages()).isEqualTo(infoPage.getInfoMessages());
        //assertThat(result.getErrorMessages()).isEqualTo(infoPage.getErrorMessages());
    }
}
