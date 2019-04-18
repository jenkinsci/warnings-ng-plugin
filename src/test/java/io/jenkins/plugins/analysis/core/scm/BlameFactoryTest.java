package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import jenkins.triggers.SCMTriggerItem;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.scm.BlamerAssert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BlameFactory}.
 *
 * @author Andreas Reiser
 */
class BlameFactoryTest {
    @Test
    void shouldCreateNullBlamerOnMissingGitPlugin() {
        enableGitPlugin(false);

        assertThatBlamer(mock(Run.class)).isInstanceOf(NullBlamer.class);
    }

    @Test
    void shouldCreateNullBlamerOnNullScm() {
        enableGitPlugin(true);

        assertThatBlamer(mock(Run.class)).isInstanceOf(NullBlamer.class);
    }

    @Test
    void shouldCreateGitBlamerOnGitScm() {
        enableGitPlugin(true);

        AbstractProject job = mock(AbstractProject.class);
        GitSCM gitScm = createGitStub();
        when(job.getScm()).thenReturn(gitScm);

        AbstractBuild build = createBuildFor(job);
        assertThatBlamer(build).isInstanceOf(GitBlamer.class);
    }

    @Test
    void shouldCreateGitBlamerOnGitScmOnRoot() {
        enableGitPlugin(true);

        AbstractProject job = mock(AbstractProject.class);

        AbstractProject root = mock(AbstractProject.class);
        GitSCM gitScm = createGitStub();
        when(root.getScm()).thenReturn(gitScm);

        when(job.getRootProject()).thenReturn(root);

        AbstractBuild build = createBuildFor(job);
        assertThatBlamer(build).isInstanceOf(GitBlamer.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateGitBlamerForPipeline() {
        enableGitPlugin(true);

        Job pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        GitSCM gitScm = createGitStub();
        when(((SCMTriggerItem) pipeline).getSCMs()).thenReturn(asSingleton(gitScm));

        Run<?, ?> run = createRunFor(pipeline);
        assertThatBlamer(run).isInstanceOf(GitBlamer.class);
    }

    @Test
    void shouldCreateNullBlamerForPipelineWithNoScm() {
        enableGitPlugin(true);

        Job pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        GitSCM gitScm = createGitStub();
        when(((SCMTriggerItem) pipeline).getSCMs()).thenReturn(new ArrayList<>());

        Run<?, ?> run = createRunFor(pipeline);
        assertThatBlamer(run).isInstanceOf(NullBlamer.class);
    }

    private List asSingleton(final GitSCM gitScm) {
        return Collections.singletonList(gitScm);
    }

    @Test
    void shouldCreateNullBlamerIfNeitherProjectNorRootHaveScm() {
        enableGitPlugin(true);

        AbstractProject job = mock(AbstractProject.class);

        AbstractProject root = mock(AbstractProject.class);
        when(job.getRootProject()).thenReturn(root);

        AbstractBuild build = createBuildFor(job);

        assertThatBlamer(build).isInstanceOf(NullBlamer.class);
    }

    private BlamerAssert assertThatBlamer(final Run run) {
        return assertThat(BlameFactory.createBlamer(run, null, TaskListener.NULL));
    }

    private Run<?, ?> createRunFor(final Job<?, ?> job) {
        Run build = mock(Run.class);
        createRunWithEnvironment(job, build, build.getParent());
        return build;
    }

    private AbstractBuild createBuildFor(final AbstractProject job) {
        AbstractBuild build = mock(AbstractBuild.class);
        createRunWithEnvironment(job, build, build.getProject());
        return build;
    }

    private void createRunWithEnvironment(final Job<?, ?> job, final Run build, final Job parent) {
        try {
            EnvVars environment = mock(EnvVars.class);
            when(build.getEnvironment(TaskListener.NULL)).thenReturn(environment);

            when(build.getParent()).thenReturn(job);
        }
        catch (IOException | InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private GitSCM createGitStub() {
        try {
            DescribableList<GitSCMExtension, GitSCMExtensionDescriptor> listStub = mock(DescribableList.class);

            GitSCM gitScm = mock(GitSCM.class);
            when(gitScm.getExtensions()).thenReturn(listStub);

            GitClient gitClient = mock(GitClient.class);
            when(gitScm.createClient(any(), any(), any(), any())).thenReturn(gitClient);

            return gitScm;
        }
        catch (IOException | InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }

    private void enableGitPlugin(final boolean gitInstalled) {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.isPluginInstalled("git")).thenReturn(gitInstalled);
        BlameFactory.setJenkinsFacade(jenkinsFacade);
    }
}