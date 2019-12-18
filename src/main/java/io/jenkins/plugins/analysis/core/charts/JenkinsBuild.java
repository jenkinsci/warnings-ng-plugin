package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.echarts.Build;

import hudson.model.Run;

/**
 * A build that has been executed by a Jenkins {@link Run}.
 *
 * @author Ullrich Hafner
 */
public class JenkinsBuild extends Build {
    /**
     * Creates a new build.
     *
     * @param build
     *         the Jenkins build
     */
    public JenkinsBuild(final Run<?, ?> build) {
        super(build.getNumber(), build.getDisplayName(), (int) (build.getTimeInMillis() / 1000));
    }
}
