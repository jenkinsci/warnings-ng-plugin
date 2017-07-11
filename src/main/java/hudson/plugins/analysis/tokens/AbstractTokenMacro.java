package hudson.plugins.analysis.tokens;

import java.io.IOException;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Provides a token that evaluates to the plug-in build result.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractTokenMacro extends DataBoundTokenMacro {
    private final Class<? extends ResultAction<? extends BuildResult>>[] resultActions;
    private final String tokenName;

    /**
     * Creates a new instance of {@link AbstractTokenMacro}.
     *
     * @param tokenName
     *            the name of the token
     * @param resultActions
     *            associated action types containing the build result
     */
    public AbstractTokenMacro(final String tokenName,
            final Class<? extends ResultAction<? extends BuildResult>>... resultActions) {
        super();

        this.resultActions = resultActions;
        this.tokenName = tokenName;
    }

    @Override
    public String evaluate(final AbstractBuild<?, ?> context, final TaskListener listener, final String macroName)
            throws MacroEvaluationException, IOException, InterruptedException {
        return evaluate(context);
    }

    @Override
    public String evaluate(final Run<?, ?> context, FilePath workspace, final TaskListener listener, final String macroName)
            throws MacroEvaluationException, IOException, InterruptedException {
        return evaluate(context);
    }

    private String evaluate(final Run<?, ?> context) {
        for (Class<? extends ResultAction<? extends BuildResult>> resultActionType : resultActions) {
            ResultAction<? extends BuildResult> action = context.getAction(resultActionType);
            if (action != null) {
                return evaluate(action.getResult());
            }
        }
        return "";
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

