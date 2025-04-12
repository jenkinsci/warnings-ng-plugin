package io.jenkins.plugins.analysis.warnings.integrations;

import org.junit.jupiter.api.Test;

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
class TokenMacroITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Runs a pipeline and verifies the expansion of token ANALYSIS_ISSUES_COUNT with the token-macro plugin.
     */
    @Test
    void shouldExpandTokenMacro() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml", "checkstyle2.xml");

        configureToken(job, "checkstyle1");

        var baseline = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        verifyConsoleLog(baseline, 3, 0, 0);

        configureToken(job, "checkstyle2");

        var result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        verifyConsoleLog(result, 4, 3, 2);
    }

    /**
     * Runs a pipeline and verifies the expansion of tokens for different severities.
     */
    @Test
    void shouldExpandDifferentSeverities() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("all-severities.xml");

        job.setDefinition(createPipelineScript("""
                node {
                  stage ('Integration Test') {
                         recordIssues tool: checkStyle(pattern: '**/\
                all-severities\
                *')
                         def total = tm('${ANALYSIS_ISSUES_COUNT}')
                         def error = tm('${ANALYSIS_ISSUES_COUNT, type="TOTAL_ERROR"}')
                         def high = tm('${ANALYSIS_ISSUES_COUNT, type="TOTAL_HIGH"}')
                         def normal = tm('${ANALYSIS_ISSUES_COUNT, type="TOTAL_NORMAL"}')
                         def low = tm('${ANALYSIS_ISSUES_COUNT, type="TOTAL_LOW"}')
                         echo '[total=' + total + ']'\s
                         echo '[error=' + error + ']'\s
                         echo '[high=' + high + ']'\s
                         echo '[normal=' + normal + ']'\s
                         echo '[low=' + low + ']'\s
                  }
                }\
                """));

        var baseline = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(baseline).hasTotalSize(3);

        assertThat(baseline).hasTotalHighPrioritySize(0);
        assertThat(baseline).hasTotalErrorsSize(1);
        assertThat(baseline).hasTotalNormalPrioritySize(1);
        assertThat(baseline).hasTotalLowPrioritySize(1);

        assertThat(getConsoleLog(baseline)).contains("[total=" + 3 + "]");

        assertThat(getConsoleLog(baseline)).contains("[error=" + 1 + "]");
        assertThat(getConsoleLog(baseline)).contains("[high=" + 0 + "]");
        assertThat(getConsoleLog(baseline)).contains("[normal=" + 1 + "]");
        assertThat(getConsoleLog(baseline)).contains("[low=" + 1 + "]");
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
        job.setDefinition(createPipelineScript("node {\n"
                + "  stage ('Integration Test') {\n"
                + "         discoverReferenceBuild()\n"
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
                + "}"));
    }
}
