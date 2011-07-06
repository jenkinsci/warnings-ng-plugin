package hudson.plugins.analysis.tokens;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Provides a token that evaluates to the plug-in build result.
 *
 * @author Ulli Hafner
 */
public class AbstractResultTokenMacro extends AbstractTokenMacro {
    /**
     * Creates a new instance of {@link AbstractResultTokenMacro}.
     * @param tokenName
     *            the name of the token
     * @param resultActions
     *            associated actions containing the build result
     */
    public AbstractResultTokenMacro(final String tokenName,
            final Class<? extends ResultAction<? extends BuildResult>>... resultActions) {
        super(tokenName, resultActions);
    }

    @Override
    protected String evaluate(final BuildResult result) {
        return result.getPluginResult().toString();
    }
}

