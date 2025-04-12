package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.ToolInstallations;

import edu.hm.hafner.analysis.Severity;

import hudson.model.Result;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.MavenConsole;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in maven jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
class MavenIssuesRecorderITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     */
    @Test
    void shouldCreateResultWithWarnings() {
        var project = createMavenJob();
        copySingleFileToWorkspace(project, "pom.xml");
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse.txt");
        enableEclipseWarnings(project);

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasNewSize(0);
        assertThat(result).hasInfoMessages(
                "-> resolved module names for 8 issues",
                "-> resolved package names of 4 affected files");
    }

    /**
     * Runs a maven build without a pom.xml. Enables reporting of maven warnings and errors.
     */
    @Test
    void shouldParseMavenError() {
        var project = createMavenJob();
        copySingleFileToWorkspace(project, "pom-error.xml", "pom.xml");

        var recorder = enableWarnings(project, createTool(new MavenConsole(), ""));
        recorder.setEnabledForFailure(true);

        var result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result).hasTotalSize(2).hasTotalErrorsSize(2);
        assertThat(result.getSizePerSeverity()).contains(entry(Severity.ERROR, 2));
    }

    /**
     * Ensures that Maven 3.5 is installed before a test will be executed.
     */
    @BeforeAll
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock", "PMD.AvoidCatchingGenericException"})
    public static void installMaven() {
        try {
            ToolInstallations.configureMaven35();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
