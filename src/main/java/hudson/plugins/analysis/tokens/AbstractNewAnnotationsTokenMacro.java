package hudson.plugins.analysis.tokens;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Provides a token that evaluates to the number of new annotations found by a
 * plug-in.
 *
 * @author Ulli Hafner
 */
public class AbstractNewAnnotationsTokenMacro extends AbstractTokenMacro {
    /**
     * Creates a new instance of {@link AbstractNewAnnotationsTokenMacro}.
     * @param tokenName
     *            the name of the token
     * @param resultActions
     *            associated actions containing the build result
     */
    public AbstractNewAnnotationsTokenMacro(final String tokenName,
            final Class<? extends ResultAction<? extends BuildResult>>... resultActions) {
        super(tokenName, resultActions);
    }

    @Override
    protected String evaluate(final BuildResult result) {
        return String.valueOf(result.getNumberOfNewWarnings());
    }
}

