package io.jenkins.plugins.analysis.warnings;

import java.io.File;

import org.junit.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Severity;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import hudson.FilePath;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.analysis.core.model.AnalysisResultAssert.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for the use of the warnings-ng plugin together with the timestamper plugin.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
public class TimestampPluginITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Tests for the correct parsing of javac warnings with enabled timestamper plugin.
     */
    @Test
    public void shouldCorrectlyParseJavacErrors() {
        WorkflowJob project = createPipeline();

        createFileInWorkspace(project, "Test.java", "public class Test {}");

        project.setDefinition(new CpsFlowDefinition("node {\n"
                + "    timestamps {\n"
                + "        echo '[javac] Test.java:39: warning: Test Warning'\n"
                + "        recordIssues tools: [java()]\n"
                + "    }\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(project);

        assertThat(result).hasTotalSize(1);
        assertThat(result).hasNoErrorMessages();

        Issue issue = result.getIssues().get(0);
        assertThat(new FilePath(new File(issue.getFileName()))).isEqualTo(getWorkspace(project).child("Test.java"));
        assertThat(issue.getLineStart()).isEqualTo(39);
        assertThat(issue.getLineEnd()).isEqualTo(39);
        assertThat(issue.getMessage()).isEqualTo("Test Warning");
        assertThat(issue.getSeverity()).isEqualTo(Severity.WARNING_NORMAL);
    }

    /**
     * Tests JENKINS-56484: Error while parsing clang errors with active timestamper plugin.
     */
    @Test
    public void shouldCorrectlyParseClangErrors() {
        WorkflowJob project = createPipeline();

        createFileInWorkspace(project, "test.c", "int main(void) { }");

        project.setDefinition(new CpsFlowDefinition("node {\n"
                + "    timestamps {\n"
                + "        echo 'test.c:1:2: error: This is an error.'\n"
                + "        recordIssues tools: [clang(id: 'clang', name: 'clang')]\n"
                + "    }\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(project);

        assertThat(result).hasTotalSize(1);
        assertThat(result).hasNoErrorMessages();

        Issue issue = result.getIssues().get(0);
        assertThat(new FilePath(new File(issue.getFileName()))).isEqualTo(getWorkspace(project).child("test.c"));
        assertThat(issue.getLineStart()).isEqualTo(1);
        assertThat(issue.getLineEnd()).isEqualTo(1);
        assertThat(issue.getMessage()).isEqualTo("This is an error.");
        assertThat(issue.getSeverity()).isEqualTo(Severity.WARNING_HIGH);
    }
}

