package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.gitclient.GitClient;
import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.Saveable;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.impl.CloneOption;
import hudson.scm.NullSCM;
import hudson.util.DescribableList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link GitChecker}.
 *
 * @author Andreas Pabst
 */
class GitCheckerTest {

    @Test
    void shouldDetermineIfSCMIsGit() {
        GitChecker gitChecker = new GitChecker();

        assertThat(gitChecker.isGit(new GitSCM(null))).isTrue();
        assertThat(gitChecker.isGit(new NullSCM())).isFalse();
    }

    @Test
    void shouldCreateBlamer() throws Exception {
        PrintStream logger = mock(PrintStream.class);
        TaskListener taskListener = mock(TaskListener.class);
        when(taskListener.getLogger()).thenReturn(logger);

        DescribableList describableList = mock(DescribableList.class);
        GitSCM gitSCM = mock(GitSCM.class);
        when(gitSCM.getExtensions()).thenReturn(describableList);

        Run run = mock(Run.class);
        EnvVars envVars = new EnvVars();
        envVars.put("GIT_COMMIT", "test_commit");
        when(run.getEnvironment(taskListener)).thenReturn(envVars);

        GitClient gitClient = mock(GitClient.class);

        when(gitSCM.createClient(taskListener, envVars, run, null)).thenReturn(gitClient);

        GitChecker gitChecker = new GitChecker();
        Blamer blamer = gitChecker.createBlamer(run, gitSCM, null, taskListener);
        assertThat(blamer).isInstanceOf(GitBlamer.class);
    }

    @Test
    void shouldCreateNullBlamerOnShallowGit() {
        PrintStream logger = mock(PrintStream.class);
        TaskListener taskListener = mock(TaskListener.class);
        when(taskListener.getLogger()).thenReturn(logger);

        CloneOption shallowCloneOption = mock(CloneOption.class);
        when(shallowCloneOption.isShallow()).thenReturn(true);

        GitSCM gitSCM = mock(GitSCM.class);
        when(gitSCM.getExtensions()).thenReturn(new DescribableList(Saveable.NOOP, Lists.list(shallowCloneOption)));

        Run run = mock(Run.class);
        GitChecker gitChecker = new GitChecker();
        assertThat(gitChecker.createBlamer(run, gitSCM, null, taskListener)).isInstanceOf(NullBlamer.class);
    }

    @Test
    void shouldCreateNullBlamerOnError() throws Exception {
        GitChecker gitChecker = new GitChecker();
        TaskListener taskListener = mock(TaskListener.class);
        Run run = mock(Run.class);
        List<GitSCMExtension> extensions = new ArrayList<>();
        GitSCM gitSCM = new GitSCM(null, null, false, null, null, null, extensions);

        when(run.getEnvironment(taskListener)).thenThrow(new IOException());
        assertThat(gitChecker.createBlamer(run, gitSCM, null, taskListener)).isInstanceOf(NullBlamer.class);
    }
}
