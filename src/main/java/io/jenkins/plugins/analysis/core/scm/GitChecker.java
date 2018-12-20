package io.jenkins.plugins.analysis.core.scm;

import java.io.IOException;

import org.jenkinsci.plugins.gitclient.GitClient;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.impl.CloneOption;
import hudson.scm.SCM;

/**
 * Facade for git API calls. Make sure that each method call in this class is wrapped into the following snippet so that
 * no {@link ClassNotFoundException} is thrown if the git plug-in is not installed or disabled:
 * <blockquote><pre>
 * Jenkins instance = Jenkins.getInstance();
 * if (instance.getPlugin("git") != null) {
 *     GitChecker gitChecker = new GitChecker();
 *     ... call method on gitChecker ...
 *  }
 * </pre></blockquote>
 *
 * @author Ullrich Hafner
 */
// TODO: Whom should we blame if the whole file is marked? Or if a range is marked and multiple authors are in the range
// TODO: Links in commits?
// TODO: Check if we should also create new Jenkins users
// TODO: Blame needs only run for new warnings
class GitChecker {
    /**
     * Returns whether the specified SCM is git.
     *
     * @param scm
     *         the SCM to test
     *
     * @return {@code true} if the SCM is git, {@code false} otherwise
     */
    boolean isGit(final SCM scm) {
        return scm instanceof GitSCM;
    }

    /**
     * Returns a Git blamer for the specified build and SCM instance.
     *
     * @param build
     *         the build to get the results for
     * @param scm
     *         the SCM instance
     * @param workspace
     *         current workspace
     * @param listener
     *         task listener
     *
     * @return {@code true} new users can be created automatically, {@code false} otherwise
     */
    Blamer createBlamer(final Run<?, ?> build, final SCM scm,
            final FilePath workspace, final TaskListener listener) {
        try {
            GitSCM gitSCM = asGit(scm);
            if (isShallow(gitSCM)) {
                listener.getLogger().println("Skipping issues blame since Git has been configured with shallow clone.");
                return new NullBlamer();
            }
            EnvVars environment = build.getEnvironment(listener);
            GitClient gitClient = gitSCM.createClient(listener, environment, build, workspace);
            String gitCommit = environment.getOrDefault("GIT_COMMIT", "HEAD");

            return new GitBlamer(gitClient, gitCommit);
        }
        catch (IOException | InterruptedException e) {
            return new NullBlamer();
        }
    }

    private boolean isShallow(final GitSCM git) {
        CloneOption option = git.getExtensions().get(CloneOption.class);
        if (option != null) {
            return option.isShallow();
        }
        return false;
    }

    private GitSCM asGit(final SCM scm) {
        return (GitSCM) scm;
    }
}
