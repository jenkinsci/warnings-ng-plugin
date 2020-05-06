package io.jenkins.plugins.analysis.warnings.plugins;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
import io.jenkins.plugins.grading.AutoGrader;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class AutogradingPluginITest extends IntegrationTestWithJenkinsPerSuite {

    private static final String ANALYSIS_RESULT = "{\"analysis\":{\"maxScore\":100,\"errorImpact\":-10,\"highImpact\":-5,\"normalImpact\":-2,\"lowImpact\":-1}}";

    @Test
    public void shouldCheckAnalysisResultsCheckstyle() {
        FreeStyleProject project = createJavaWarningsFreestyleProject("checkstyle");

        IssuesRecorder recorder = new IssuesRecorder();
        CheckStyle checkStyle = new CheckStyle();
        checkStyle.setPattern("**/*checkstyle*");
        recorder.setTools(checkStyle);

        project.getPublishersList().add(recorder);
        project.getPublishersList().add(new AutoGrader(ANALYSIS_RESULT));

        Run<?, ?> baseline = buildSuccessfully(project);

        assertThat(getConsoleLog(baseline)).contains("[Autograding] Grading static analysis results for CheckStyle");
        assertThat(getConsoleLog(baseline)).contains("[Autograding] -> Score -60 (warnings distribution err:6, high:0, normal:0, low:0)");
        assertThat(getConsoleLog(baseline)).contains("[Autograding] Total score for static analysis results: 40");

    }

    private FreeStyleProject createJavaWarningsFreestyleProject(final String file) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(file + ".xml");
        Java java = new Java();
        java.setPattern("**/*" + file + "*");
        enableWarnings(project, java);
        return project;
    }
}
