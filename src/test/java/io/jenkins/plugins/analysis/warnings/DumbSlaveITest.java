package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.FilePath;
import hudson.model.Label;
import hudson.model.TopLevelItem;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.slaves.DumbSlave;
import jenkins.security.s2m.AdminWhitelistRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.model.AnalysisResultAssert.*;

/**
 * Integration tests for builds running on a 'DumbSlave' worker node.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
public class DumbSlaveITest extends IntegrationTestWithJenkinsPerTest {

    /**
     * Integration test to verify the base behaviour of building on a slave.
     *
     * @throws Exception
     *         if the slave can not be created
     */
    @Test
    public void shouldBuildOnSlave() throws Exception {

        DumbSlave slave = getJenkins().createOnlineSlave(Label.get("slave"));

        HudsonPrivateSecurityRealm securityRealm = new HudsonPrivateSecurityRealm(false, false, null);
        securityRealm.createAccount("user", "password");

        getJenkins().jenkins.setSecurityRealm(securityRealm);
        getJenkins().jenkins.setAuthorizationStrategy(new FullControlOnceLoggedInAuthorizationStrategy());

        getJenkins().jenkins.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
        getJenkins().jenkins.save();

        WorkflowJob project = createPipeline();
        createFileInSlaveWorkspace(slave, project, "Test.java", "public class Test {}");
        createFileInSlaveWorkspace(slave, project, "MediaPortal.cs", "hallo welt");

        project.setDefinition(new CpsFlowDefinition("node('slave') {\n"
                + "    echo '[javac] Test.java:39: warning: Test Warning'\n"
                + "    echo 'MediaPortal.cs(3001,5): warning CS0162: Hier kommt der Warnings Text'\n"
                + "    recordIssues tools: [msBuild(), java()]\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(project);

        assertThat(result).hasNoErrorMessages();
    }

    private void createFileInSlaveWorkspace(final DumbSlave slave, final TopLevelItem job, final String fileName,
            final String content) {
        try {
            FilePath workspace = slave.getWorkspaceFor(job);

            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }
        catch (IOException | InterruptedException | NullPointerException e) {
            throw new AssertionError(e);
        }
    }
}
