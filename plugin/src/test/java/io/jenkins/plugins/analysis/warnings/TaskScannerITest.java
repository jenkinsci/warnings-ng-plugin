package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static org.assertj.core.api.Assertions.*;

public class TaskScannerITest extends IntegrationTestWithJenkinsPerTest {

    @Test
    public void shouldNotFindFile() {
        WorkflowJob job = createPipelineWithWorkspaceFiles();

        configureScanner(job, "non-existing-file.txt");
        Run<?, ?> baseline = buildSuccessfully(job);

        assertThat(getConsoleLog(baseline)).isNotEmpty();
        assertThat(getConsoleLog(baseline)).contains("[-ERROR-] No files found for pattern '**/non-existing-file.txt*'. Configuration error?");
    }


    @Test
    public void shouldParseNonUtf8File() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("tasks/checkstyle-with-windows-encoding.txt");

        configureScanner(job, "checkstyle-with-windows-encoding");
        Run<?, ?> baseline = buildSuccessfully(job);

        assertThat(getConsoleLog(baseline)).isNotEmpty();
        assertThat(getConsoleLog(baseline)).contains("Successfully parsed file ");
    }


    private void configureScanner(final WorkflowJob job, final String fileName) {
        job.setDefinition(new CpsFlowDefinition("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         def report = scanForIssues tool: checkStyle(pattern: '**/" + fileName + "*')\n"
                + "         echo '[total=' + report.size() + ']' \n"
                + "         echo '[id=' + report.getId() + ']' \n"
                + "         def issues = report.getIssues()\n"
                + "         issues.each { issue ->\n"
                + "             echo issue.toString()\n"
                + "             echo issue.getOrigin()\n"
                + "             echo issue.getAuthorName()\n"
                + "         }"
                + "  }\n"
                + "}", true));
    }
}
