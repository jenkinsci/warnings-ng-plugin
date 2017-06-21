package hudson.plugins.analysis.util;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;

/**
 * Facade for git API calls. Make sure that each method call in this class is wrapped into the following snippet so
 * that no {@link ClassNotFoundException} is thrown if the git plug-in is not installed or disabled:
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
// First Release
// TODO: ATH in docker container to make sure master slave works
// TODO: Authors: match by name only (not email)
// TODO: check why build number is 0 (in first build it is 1)
// TODO: Tooltip im Graph?
// TODO: Lange Namen im Graph?
// Second Release
// TODO: Make trend graph clickable per user
// TODO: Commit tab?
// TODO: Links in commits?
// TODO: Check if we should also create new Jenkins users
public class GitChecker {
    /**
     * Returns whether the specified SCM is git.
     *
     * @param scm the SCM to test
     * @return {@code true} if the SCM is git, {@code false} otherwise
     */
    public boolean isGit(final SCM scm) {
        return scm instanceof GitSCM;
    }

    /**
     * Returns a Git blamer for the specified build and SCM instance.
     *
     * @param scm       the SCM instance
     * @param build     the current build
     * @param listener  task listener
     * @param logger    plugin logger
     * @param workspace current workspace
     * @return {@code true} new users can be created automatically, {@code false} otherwise
     */
    public Blamer createBlamer(final AbstractBuild build, final SCM scm, final FilePath workspace,
            final PluginLogger logger, final TaskListener listener) {
        return new GitBlamer(build, asGit(scm), workspace, logger, listener);
    }

    private GitSCM asGit(final SCM scm) {
        return (GitSCM) scm;
    }
}
