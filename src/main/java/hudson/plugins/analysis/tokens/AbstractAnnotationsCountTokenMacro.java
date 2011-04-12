package hudson.plugins.analysis.tokens;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Provides a token that evaluates to the number of annotations found by a
 * plug-in.
 *
 * @author Ulli Hafner
 */
public class AbstractAnnotationsCountTokenMacro extends AbstractTokenMacro {
    /**
     * Creates a new instance of {@link AbstractAnnotationsCountTokenMacro}.
     *
     * @param resultAction
     *            the associated action containing the build result
     * @param tokenName
     *            the name of the token
     */
    public AbstractAnnotationsCountTokenMacro(final Class<? extends ResultAction<? extends BuildResult>> resultAction,
            final String tokenName) {
        super(resultAction, tokenName);
    }

    @Override
    protected String evaluate(final BuildResult result) {
        return String.valueOf(result.getNumberOfAnnotations());
    }
}

