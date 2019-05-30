package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Result;
import hudson.model.Slave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsTab.TabType;

import static io.jenkins.plugins.analysis.core.testutil.IntegrationTest.JavaScriptSupport.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the remote functionality of the plugin.
 *
 * @author Veronika Zwickenpflug
 * @author Florian Hageneder
 */
public class RemoteITest extends IntegrationTestWithJenkinsPerTest {

    /**
     * Lets a pipeline run on an agent who has to copy an file with issues back to the master.
     */
    @Test
    public void shouldCopyFilesToMaster() {
        WorkflowJob job = initAgentAndProject("OpenTasks.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(result.getErrorMessages()).isEmpty();
        assertThat(result.getInfoMessages()).contains(
                "Found a total of 3 open tasks",
                "-> 1 copied, 0 not in workspace, 0 not-found, 0 with I/O error"
        );
    }

    /**
     * Lets a pipeline run on an agents and check if the details tab work still correctly.
     */
    @Test
    public void detailsTabShouldWorkCorrectly() {
        WorkflowJob job = initAgentAndProject("OpenTasks.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        DetailsTab detailsTab = new DetailsTab(getWebPage(JS_ENABLED, result));
        assertThat(detailsTab.getTabTypes()).containsExactly(TabType.TYPES, TabType.ISSUES);
    }

    /**
     * Lets a pipeline run on an agent who has to copy an file with issues back to the master with set security realm.
     * Activated security is assured with {@link #initAgentAndProject(String)} using {@link
     * #createAgentWithEnabledSecurity(String)}.
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-56007">Issue 56007</a>
     */
    @Test
    @Issue("JENKINS-56007")
    public void shouldCopyFilesToMasterWithSecurity() {
        WorkflowJob job = initAgentAndProject("OpenTasks.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(result.getErrorMessages()).isEmpty();
        assertThat(result.getInfoMessages()).contains(
                "Found a total of 3 open tasks",
                "-> 1 copied, 0 not in workspace, 0 not-found, 0 with I/O error"
        );
    }

    /**
     * Initializes an agent and creates a pipeline that uses this agent. Also copies given file to workspace.
     *
     * @param file
     *         File to be copied to workspace of the created pipeline
     *
     * @return Created pipeline
     */
    private WorkflowJob initAgentAndProject(final String file) {
        Slave agent = createAgentWithEnabledSecurity("restricted-agent");
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "    agent { label 'restricted-agent' }\n"
                + "    stages {\n"
                + "        stage ('Record Issues') {\n"
                + "            steps {\n"
                + "                recordIssues(tools: [taskScanner(highTags: 'FIXMEE', normalTags: 'TODOO')])\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}", true));

        copySingleFileToAgentWorkspace(agent, job, file, file);
        return job;
    }

}
