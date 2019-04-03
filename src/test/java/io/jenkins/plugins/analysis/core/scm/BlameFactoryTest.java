package io.jenkins.plugins.analysis.core.scm;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static org.mockito.Mockito.*;

class BlameFactoryTest {

    private JenkinsFacade jenkinsFacadeMock = mock(JenkinsFacade.class);
    private Run runMock = mock(Run.class);
    private GitChecker gitCheckerMock = mock(GitChecker.class);

    @AfterEach
    void resetBlameFactory() {
        BlameFactory.setGitChecker(new GitChecker());
        BlameFactory.setJenkinsFacade(new JenkinsFacade());
    }

    @Test
    void shouldCreateNullBlamerOnMissingGitPlugin() {
        when(jenkinsFacadeMock.isPluginInstalled("git")).thenReturn(false);
        BlameFactory.setJenkinsFacade(jenkinsFacadeMock);
        Assert.assertTrue("should create a new null blamer",
                BlameFactory.createBlamer(null, null, TaskListener.NULL) instanceof NullBlamer
        );
    }

    @Test
    void shouldCreateNullBlamerOnNullScm() {
        when(jenkinsFacadeMock.isPluginInstalled("git")).thenReturn(true);
        when(runMock.getParent()).thenReturn(null);
        BlameFactory.setJenkinsFacade(jenkinsFacadeMock);
        Assert.assertTrue("should create a new null blamer",
                BlameFactory.createBlamer(runMock, null, TaskListener.NULL) instanceof NullBlamer
        );
    }

    @Test
    void shouldCreateNullBlamerOnWrongScm() {

        //Can't mock workflojob because of final class,
        // so let's mock the method used internally instead...
        Job workFlowJobMock = mock(Job.class);
        when(workFlowJobMock.getLastSuccessfulBuild()).thenReturn(runMock);

        when(jenkinsFacadeMock.isPluginInstalled("git")).thenReturn(true);
        when(runMock.getParent()).thenReturn(workFlowJobMock);
        BlameFactory.setJenkinsFacade(jenkinsFacadeMock);
        Assert.assertTrue("should create a new git blamer",
                BlameFactory.createBlamer(runMock, null, TaskListener.NULL) instanceof NullBlamer
        );
    }

    @Test
    void shouldCreateGitBlamerOnGitScm() {

        AbstractBuild abstractBuildMock = mock(AbstractBuild.class);

        when(jenkinsFacadeMock.isPluginInstalled("git")).thenReturn(true);
        BlameFactory.setJenkinsFacade(jenkinsFacadeMock);
        AbstractProject projectMock = mock(AbstractProject.class);
        SCM gitScm = new GitSCM("Here be git");
        when(projectMock.getScm()).thenReturn(gitScm);

        when(abstractBuildMock.getProject()).thenReturn(projectMock);
        when(gitCheckerMock.isGit(any())).thenReturn(true);

        BlameFactory.setGitChecker(gitCheckerMock);
        BlameFactory.createBlamer(abstractBuildMock, null, TaskListener.NULL);
        verify(gitCheckerMock).createBlamer(abstractBuildMock, gitScm, null, TaskListener.NULL);
    }

}