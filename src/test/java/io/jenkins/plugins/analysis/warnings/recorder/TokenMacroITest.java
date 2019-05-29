package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the expansion of the token-macro plugin.
 *
 * @author Colin Kaschel
 * @author Nils Engelbrecht
 */
public class TokenMacroITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Runs a pipeline and verifies the expansion of token ANALYSIS_ISSUES_COUNT with the token-macro plugin.
     */
    @Test
    public void shouldExpandTokenMacro() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("../java1Warning.txt");

        job.setDefinition(new CpsFlowDefinition("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         recordIssues tool: java(pattern: '**/*txt')\n"
                + "         def total = tm('${ANALYSIS_ISSUES_COUNT}')\n"
                + "         echo '[total=' + total + ']' \n"
                + "  }\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertThat(getConsoleLog(result)).contains("[total=1]");
    }
}
