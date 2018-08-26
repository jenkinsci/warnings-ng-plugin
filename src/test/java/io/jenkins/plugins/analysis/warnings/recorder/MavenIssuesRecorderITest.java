package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;
import org.jvnet.hudson.test.ToolInstallations;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import hudson.maven.MavenModuleSet;
import hudson.model.Result;

/**
 * Integration tests of the warnings plug-in in maven jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class MavenIssuesRecorderITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     */
    @Test
    public void shouldCreateResultWithWarnings() {
        installMaven();
        
        MavenModuleSet project = createMavenJob();
        copySingleFileToWorkspace(project, "pom.xml");
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse.txt");
        enableEclipseWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasNewSize(0);
        assertThat(result).hasInfoMessages(
                "-> resolved module names for 8 issues",
                "-> resolved package names of 4 affected files");
    }

    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock", "PMD.AvoidCatchingGenericException"})
    private void installMaven() {
        try {
            ToolInstallations.configureMaven35();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}