package hudson.plugins.warnings.tokens;

import hudson.Extension;

import hudson.plugins.analysis.tokens.AbstractAnnotationsCountTokenMacro;
import hudson.plugins.warnings.WarningsResultAction;

/**
 * Provides a token that evaluates to the number of compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class WarningsCountTokenMacro extends AbstractAnnotationsCountTokenMacro {
    /**
     * Creates a new instance of {@link WarningsCountTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public WarningsCountTokenMacro() {
        super("WARNINGS_COUNT", WarningsResultAction.class);
    }
}

