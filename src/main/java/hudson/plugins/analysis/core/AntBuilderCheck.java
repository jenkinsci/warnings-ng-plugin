package hudson.plugins.analysis.core;

import hudson.model.AbstractBuild;
import hudson.model.Project;

import hudson.tasks.Builder;
import hudson.tasks.Ant;

/**
 * Verifies if the build is an {@link Ant} task.
 *
 * @author Ulli Hafner
 */
public final class AntBuilderCheck {
    /**
     * Returns whether the current build uses ant.
     *
     * @param build
     *            the current build
     * @return <code>true</code> if the current build uses ant,
     *         <code>false</code> otherwise
     */
    public static boolean isAntBuild(final AbstractBuild<?, ?> build) {
        if (build.getProject() instanceof Project) {
            Project<?, ?> project = (Project<?, ?>)build.getProject();
            for (Builder builder : project.getBuilders()) {
                if (builder instanceof Ant) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a new instance of {@link AntBuilderCheck}.
     */
    private AntBuilderCheck() {
        // prevent instantiation
    }
}

