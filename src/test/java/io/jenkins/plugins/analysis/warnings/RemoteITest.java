package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Result;
import hudson.model.Slave;
import jenkins.security.s2m.AdminWhitelistRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

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
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-56007">Issue 56007</a>
     * @throws IOException when persisting Jenkins config fails.
     */
    @Test
    public void shouldCopyFilesToMaster() throws IOException {
        // Enable Security and Master Access Control
        getJenkins().getInstance().setSecurityRealm(getJenkins().createDummySecurityRealm());
        getJenkins().getInstance().getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
        getJenkins().jenkins.save();
        assertThat(getJenkins().getInstance().isUseSecurity()).isTrue();

        Slave agent = createAgent("restricted-agent");
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
        copySingleFileToAgentWorkspace(agent, job, "OpenTasks.txt", "OpenTasks.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(result.getErrorMessages()).isEmpty();
        assertThat(result.getInfoMessages()).contains(
                "Found a total of 3 open tasks",
                "-> 1 copied, 0 not in workspace, 0 not-found, 0 with I/O error"
        );
    }

}
