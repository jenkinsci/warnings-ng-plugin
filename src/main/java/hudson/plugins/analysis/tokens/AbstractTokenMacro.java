package hudson.plugins.analysis.tokens;

import java.io.IOException;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Provides a token that evaluates to the plug-in build result.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractTokenMacro extends DataBoundTokenMacro {
    private final Class<? extends ResultAction<? extends BuildResult>> resultAction;
    private final String tokenName;

    /**
     * Creates a new instance of {@link AbstractTokenMacro}.
     *
     * @param resultAction
     *            the associated action containing the build result
     * @param tokenName
     *            the name of the token
     */
    public AbstractTokenMacro(final Class<? extends ResultAction<? extends BuildResult>> resultAction,
            final String tokenName) {
        super();

        this.resultAction = resultAction;
        this.tokenName = tokenName;
    }

    @Override
    public String evaluate(final AbstractBuild<?, ?> context, final TaskListener listener, final String macroName)
            throws MacroEvaluationException, IOException, InterruptedException {
        ResultAction<? extends BuildResult> action = context.getAction(resultAction);
        if (action == null) {
            return "";
        }

        return evaluate(action.getResult());
    }

    /**
     * Evaluates the build result and returns the string value of the token.
     *
     * @param result
     *            the result to evaluate
     * @return the string value of the token
     */
    protected abstract String evaluate(final BuildResult result);

    @Override
    public boolean acceptsMacroName(final String macroName) {
        return tokenName.equals(macroName);
    }
}

