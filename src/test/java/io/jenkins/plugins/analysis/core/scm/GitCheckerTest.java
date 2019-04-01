package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.jenkinsci.plugins.gitclient.GitClient;
import hudson.EnvVars;
import hudson.Util;
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
public class GitCheckerTest {

    @Test
    public void shouldDetermineIfSCMIsGit() {
        GitChecker gitChecker = new GitChecker();

        assertThat(gitChecker.isGit(new GitSCM(null))).isTrue();
        assertThat(gitChecker.isGit(new NullSCM())).isFalse();
    }

    @Test
    public void shouldCreateBlamer() throws Exception {
        GitChecker gitChecker = new GitChecker();
        PrintStream logger = mock(PrintStream.class);
        TaskListener taskListener = mock(TaskListener.class);
        when(taskListener.getLogger()).thenReturn(logger);

        List<GitSCMExtension> extensions = new ArrayList<>();
        GitSCM gitSCM = mock(GitSCM.class);
        when(gitSCM.getExtensions()).thenReturn(new DescribableList(Saveable.NOOP, Util.fixNull(extensions)));

        Run run = mock(Run.class);
        EnvVars envVars = new EnvVars();
        envVars.put("GIT_COMMIT", "test_commit");
        when(run.getEnvironment(taskListener)).thenReturn(envVars);

        GitClient gitClient = mock(GitClient.class);

        when(gitSCM.createClient(taskListener, envVars, run, null)).thenReturn(gitClient);

        Blamer blamer = gitChecker.createBlamer(run, gitSCM, null, taskListener);
        assertThat(blamer).isInstanceOf(GitBlamer.class);
    }

    @Test
    public void shouldCreateNullBlamerOnShallowGit() throws Exception {
        final boolean SHALLOW = true;
        GitChecker gitChecker = new GitChecker();
        PrintStream logger = mock(PrintStream.class);
        TaskListener taskListener = mock(TaskListener.class);
        when(taskListener.getLogger()).thenReturn(logger);

        List<GitSCMExtension> extensions = new ArrayList<>();
        extensions.add(new CloneOption(SHALLOW, null, null));
        GitSCM gitSCM = new GitSCM(null, null, false, null, null, null, extensions);

        Run run = mock(Run.class);
        assertThat(gitChecker.createBlamer(run, gitSCM, null, taskListener)).isInstanceOf(NullBlamer.class);
    }

    @Test
    public void shouldCreateNullBlamerOnError() throws Exception {
        GitChecker gitChecker = new GitChecker();
        TaskListener taskListener = mock(TaskListener.class);
        Run run = mock(Run.class);
        List<GitSCMExtension> extensions = new ArrayList<>();
        GitSCM gitSCM = new GitSCM(null, null, false, null, null, null, extensions);

        when(run.getEnvironment(taskListener)).thenThrow(new IOException());
        assertThat(gitChecker.createBlamer(run, gitSCM, null, taskListener)).isInstanceOf(NullBlamer.class);
    }
}
