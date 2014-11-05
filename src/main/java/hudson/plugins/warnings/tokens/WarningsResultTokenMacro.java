package hudson.plugins.warnings.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractResultTokenMacro;
import hudson.plugins.warnings.AggregatedWarningsResultAction;

/**
 * Provides a token that evaluates to the compiler warnings build result.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class WarningsResultTokenMacro extends AbstractResultTokenMacro {
    /**
     * Creates a new instance of {@link WarningsResultTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public WarningsResultTokenMacro() {
        super("WARNINGS_RESULT", AggregatedWarningsResultAction.class);
    }
}

