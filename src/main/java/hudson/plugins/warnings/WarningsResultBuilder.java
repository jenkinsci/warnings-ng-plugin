package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.JavaProject;

/**
 * Creates a new warnings result based on the values of a previous build and the
 * current project.
 *
 * @author Ulli Hafner
 */
public class WarningsResultBuilder {
    /**
     * Creates a result that persists the warnings information for the
     * specified build.
     *
     * @param build
     *            the build to create the action for
     * @param project
     *            the project containing the annotations
     * @return the result action
     */
    public WarningsResult build(final AbstractBuild<?, ?> build, final JavaProject project) {
        Object previous = build.getPreviousBuild();
        while (previous instanceof AbstractBuild<?, ?> && previous != null) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            WarningsResultAction previousAction = previousBuild.getAction(WarningsResultAction.class);
            if (previousAction != null) {
                return new WarningsResult(build, project, previousAction.getResult().getProject(),
                        previousAction.getResult().getZeroWarningsHighScore());
            }
            previous = previousBuild.getPreviousBuild();
        }
        return new WarningsResult(build, project);
    }
}

