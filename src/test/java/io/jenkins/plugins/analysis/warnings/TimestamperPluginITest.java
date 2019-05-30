package io.jenkins.plugins.analysis.warnings;

import java.io.File;

import org.junit.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.PathUtil;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.FilePath;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration test for the use of the warnings-ng plugin together with the timestamper plugin.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
public class TimestamperPluginITest extends IntegrationTestWithJenkinsPerSuite {
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
        assertThat(issue).hasFileName(getWorkspacePath(project, "Test.java"));
        assertThat(issue).hasLineStart(39);
        assertThat(issue).hasMessage("Test Warning");
        assertThat(issue).hasSeverity(Severity.WARNING_NORMAL);
    }

    private String getWorkspacePath(final WorkflowJob project, final String fileName) {
        return new PathUtil().getAbsolutePath(getWorkspace(project).child(fileName).getRemote());
    }

    /**
     * Tests JENKINS-56484: Error while parsing clang errors with active timestamper plugin.
     */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-56484")
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
        assertThat(issue).hasFileName(getWorkspacePath(project, "test.c"));
        assertThat(issue).hasLineStart(1);
        assertThat(issue).hasMessage("This is an error.");
        assertThat(issue).hasSeverity(Severity.WARNING_HIGH);
    }

    private FilePath getAbsolutePath(final Issue issue) {
        return new FilePath(new File(issue.getFileName()));
    }
}

