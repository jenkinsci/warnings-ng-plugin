package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.gitclient.GitClient;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import hudson.util.DescribableList;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.scm.BlamerAssert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BlameFactory}.
 */
class BlameFactoryTest {

    private void enableGitPlugin(final boolean gitInstalled) {
        JenkinsFacade jenkinsFacadeStub = mock(JenkinsFacade.class);
        when(jenkinsFacadeStub.isPluginInstalled("git")).thenReturn(gitInstalled);
        BlameFactory.setJenkinsFacade(jenkinsFacadeStub);
    }

    @Test
    void shouldCreateNullBlamerOnMissingGitPlugin() {
        enableGitPlugin(false);
        assertThat(BlameFactory.createBlamer(null, null, TaskListener.NULL)).isInstanceOf(NullBlamer.class);
    }

    @Test
    void shouldCreateNullBlamerOnNullScm() {
        enableGitPlugin(true);
        Run runStub = mock(Run.class);
        assertThat(BlameFactory.createBlamer(runStub, null, TaskListener.NULL)).isInstanceOf(NullBlamer.class);
    }

    @Test
    void shouldCreateNullBlamerOnWrongScm() {

        //Can't stub workflojob because of final class,
        // so let's mock the method used internally instead...
        Job workFlowJobStub = mock(Job.class);
        Run runStub = mock(Run.class);
        when(workFlowJobStub.getLastSuccessfulBuild()).thenReturn(runStub);

        enableGitPlugin(true);

        when(runStub.getParent()).thenReturn(workFlowJobStub);
        assertThat(BlameFactory.createBlamer(runStub, null, TaskListener.NULL)).isInstanceOf(NullBlamer.class);
    }

    @Test
    void shouldCreateGitBlamerOnGitScm() throws IOException, InterruptedException {

        AbstractBuild abstractBuildStub = mock(AbstractBuild.class);

        enableGitPlugin(true);
        AbstractProject projectStub = mock(AbstractProject.class);
        GitSCM gitScm = mock(GitSCM.class);
        DescribableList<GitSCMExtension, GitSCMExtensionDescriptor> listStub = mock(DescribableList.class);
        when(gitScm.getExtensions()).thenReturn(listStub);

        GitClient clientStub = mock(GitClient.class);
        when(gitScm.createClient(any(), any(), any(), any())).thenReturn(clientStub);
        when(projectStub.getScm()).thenReturn(gitScm);
        when(abstractBuildStub.getProject()).thenReturn(projectStub);

        EnvVars environmentStub = mock(EnvVars.class);
        when(abstractBuildStub.getEnvironment(TaskListener.NULL)).thenReturn(environmentStub);

        BlameFactory.createBlamer(abstractBuildStub, null, TaskListener.NULL);
    }

}