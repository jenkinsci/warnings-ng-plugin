package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.ObjectAssert;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import hudson.util.DescribableList;
import jenkins.triggers.SCMTriggerItem;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ScmResolver}.
 *
 * @author Andreas Reiser
 */
class ScmResolverTest {
    @Test
    void shouldCreateNullBlamerOnNullScm() {
        assertThatScm(mock(Run.class)).isInstanceOf(NullSCM.class);
    }

    @Test
    void shouldCreateGitBlamerOnGitScm() {
        AbstractProject job = mock(AbstractProject.class);
        GitSCM gitScm = createGitStub();
        when(job.getScm()).thenReturn(gitScm);

        AbstractBuild build = createBuildFor(job);
        assertThatScm(build).isInstanceOf(GitSCM.class);
    }

    @Test
    void shouldCreateGitBlamerOnGitScmOnRoot() {
        AbstractProject job = mock(AbstractProject.class);

        AbstractProject root = mock(AbstractProject.class);
        GitSCM gitScm = createGitStub();
        when(root.getScm()).thenReturn(gitScm);

        when(job.getRootProject()).thenReturn(root);

        AbstractBuild build = createBuildFor(job);
        assertThatScm(build).isInstanceOf(GitSCM.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateGitBlamerForPipeline() {
        Job pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        GitSCM gitScm = createGitStub();
        when(((SCMTriggerItem) pipeline).getSCMs()).thenReturn(asSingleton(gitScm));

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScm(run).isInstanceOf(GitSCM.class);
    }

    @Test @Ignore("Verify if this can be stubbed")
    void shouldCreateGitBlamerForPipelineWithFlowNode() {
        WorkflowJob pipeline = new WorkflowJob(mock(ItemGroup.class), "stub");
        CpsScmFlowDefinition flowDefinition = mock(CpsScmFlowDefinition.class);
        pipeline.setDefinition(flowDefinition);
        when(flowDefinition.getScm()).thenReturn(createGitStub());

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScm(run).isInstanceOf(GitSCM.class);
    }

    @Test
    void shouldCreateNullBlamerForPipelineWithNoScm() {
        Job pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        when(((SCMTriggerItem) pipeline).getSCMs()).thenReturn(new ArrayList<>());

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScm(run).isInstanceOf(NullSCM.class);
    }

    private List asSingleton(final GitSCM gitScm) {
        return Collections.singletonList(gitScm);
    }

    @Test
    void shouldCreateNullBlamerIfNeitherProjectNorRootHaveScm() {
        AbstractProject job = mock(AbstractProject.class);

        AbstractProject root = mock(AbstractProject.class);
        when(job.getRootProject()).thenReturn(root);

        AbstractBuild build = createBuildFor(job);

        assertThatScm(build).isInstanceOf(NullSCM.class);
    }

    private ObjectAssert<SCM> assertThatScm(final Run run) {
        return assertThat(new ScmResolver().getScm(run));
    }

    private Run<?, ?> createRunFor(final Job<?, ?> job) {
        Run build = mock(Run.class);
        createRunWithEnvironment(job, build);
        return build;
    }

    private AbstractBuild createBuildFor(final AbstractProject job) {
        AbstractBuild build = mock(AbstractBuild.class);
        createRunWithEnvironment(job, build);
        return build;
    }

    private void createRunWithEnvironment(final Job<?, ?> job, final Run build) {
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
}
