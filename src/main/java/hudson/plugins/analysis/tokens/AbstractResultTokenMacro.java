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
     *
     * @param resultAction
     *            the associated action containing the build result
     * @param tokenName
     *            the name of the token
     */
    public AbstractResultTokenMacro(final Class<? extends ResultAction<? extends BuildResult>> resultAction,
            final String tokenName) {
        super(resultAction, tokenName);
    }

    @Override
    protected String evaluate(final BuildResult result) {
        return result.getPluginResult().toString();
    }
}

