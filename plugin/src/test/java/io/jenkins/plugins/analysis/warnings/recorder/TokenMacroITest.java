package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

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
        WorkflowJob job = createPipelineWithWorkspaceFiles("checkstyle1.xml", "checkstyle2.xml");

        configureToken(job, "checkstyle1");

        AnalysisResult baseline = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        verifyConsoleLog(baseline, 3, 0, 0);

        configureToken(job, "checkstyle2");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        verifyConsoleLog(result, 4, 3, 2);
    }

    private void verifyConsoleLog(final AnalysisResult baseline, final int totalSize, final int newSize,
            final int fixedSize) {
        assertThat(baseline).hasTotalSize(totalSize);
        assertThat(baseline).hasNewSize(newSize);
        assertThat(baseline).hasFixedSize(fixedSize);

        assertThat(getConsoleLog(baseline)).contains("[total=" + totalSize + "]");
        assertThat(getConsoleLog(baseline)).contains("[checkstyle=" + totalSize + "]");
        assertThat(getConsoleLog(baseline)).contains("[broken=0]");
        assertThat(getConsoleLog(baseline)).contains("[new=" + newSize + "]");
        assertThat(getConsoleLog(baseline)).contains("[fixed=" + fixedSize + "]");
    }

    private void configureToken(final WorkflowJob job, final String fileName) {
        job.setDefinition(new CpsFlowDefinition("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         recordIssues tool: checkStyle(pattern: '**/" + fileName + "*')\n"
                + "         def total = tm('${ANALYSIS_ISSUES_COUNT}')\n"
                + "         def checkstyle = tm('${ANALYSIS_ISSUES_COUNT, tool=\"checkstyle\"}')\n"
                + "         def broken = tm('${ANALYSIS_ISSUES_COUNT, tool=\"broken\"}')\n"
                + "         def additional = tm('${ANALYSIS_ISSUES_COUNT, type=\"NEW\"}')\n"
                + "         def fixed = tm('${ANALYSIS_ISSUES_COUNT, type=\"FIXED\"}')\n"
                + "         echo '[total=' + total + ']' \n"
                + "         echo '[checkstyle=' + checkstyle + ']' \n"
                + "         echo '[broken=' + broken + ']' \n"
                + "         echo '[new=' + additional + ']' \n"
                + "         echo '[fixed=' + fixed + ']' \n"
                + "  }\n"
                + "}", true));
    }
}
