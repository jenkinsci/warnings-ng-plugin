package hudson.plugins.analysis.core;

import hudson.maven.AbstractMavenProject;

import hudson.model.AbstractProject;

/**
 * Checks if the specified project is a maven job. Since the maven plug-in is optional, this method needs to be in a
 * separate class.
 *
 * @author Ulli Hafner
 */
public final class MavenProjectChecker {
    /**
     * Returns whether the specified job type is a maven job.
     *
     * @param jobType
     *            the class of this job
     * @return <code>true</code> if the specified job type is a maven job, <code>false</code> otherwise
     */
    public static boolean isMavenProject(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
        return AbstractMavenProject.class.isAssignableFrom(jobType);
    }

    /**
     * Creates a new instance of {@link MavenProjectChecker}.
     */
    private MavenProjectChecker() {
        // prevents instantiation
    }
}
