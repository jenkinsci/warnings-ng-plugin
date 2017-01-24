package hudson.plugins.analysis.util;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;

/**
 * Creates the correct blamer for the specified SCM
 *
 * @author Lukas Krose
 */
public class BlameFactory {

    public static AbstractBlamer createBlamer(Run<?, ?> run, FilePath workspace, PluginLogger logger)
    {
        AbstractProject aProject = (AbstractProject) run.getParent();
        SCM scm = aProject.getScm();
        if(scm instanceof GitSCM)
        {
            logger.log("Found a git client");
            return new GitBlamer(run, workspace, logger);
        }

        return null;
    }
}
