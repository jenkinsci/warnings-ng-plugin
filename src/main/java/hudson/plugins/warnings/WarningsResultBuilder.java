package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.ParserResult;


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
     * @param result
     *            the result containing the annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @return the result action
     */
    public WarningsResult build(final AbstractBuild<?, ?> build, final ParserResult result, final String defaultEncoding) {
        Object previous = build.getPreviousBuild();
        while (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            WarningsResultAction previousAction = previousBuild.getAction(WarningsResultAction.class);
            if (previousAction != null) {
                return new WarningsResult(build, defaultEncoding, result, previousAction.getResult());
            }
            previous = previousBuild.getPreviousBuild();
        }
        return new WarningsResult(build, defaultEncoding, result);
    }
}

