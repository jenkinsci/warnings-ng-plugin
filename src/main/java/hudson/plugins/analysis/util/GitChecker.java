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
// TODO: Make trend graph clickable per user
// TODO: Hide trend on global settings
// TODO: Check width of warnings table
// TODO: No author in table if global settings
// TODO: Commit tab?
// TODO: Links in commits?
// TODO: ATH in docker container to make sure master slave works
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
     * Returns whether new users can be created automatically based on the blame information.
     *
     * @param scm the SCM
     * @return {@code true} new users can be created automatically, {@code false} otherwise
     */
    public boolean canCreateUsers(final SCM scm) {
        return asGit(scm).isCreateAccountBasedOnEmail();
    }

    public Blamer createBlamer(final AbstractBuild build, final SCM scm, final FilePath workspace,
            final PluginLogger logger, final TaskListener listener) {
        return new GitBlamer(build, asGit(scm), workspace, logger, listener);
    }

    private GitSCM asGit(final SCM scm) {
        return (GitSCM) scm;
    }
}
