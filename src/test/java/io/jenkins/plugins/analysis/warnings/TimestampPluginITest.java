package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test of the SourceControlTable.
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

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues().get(0).getBaseName()).isEqualTo("Test.java");
        assertThat(result.getErrorMessages()).isEmpty();
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

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getIssues().get(0).getBaseName()).isEqualTo("test.c");
        assertThat(result.getErrorMessages()).isEmpty();
    }
}

