package hudson.plugins.warnings.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractResultTokenMacro;
import hudson.plugins.warnings.WarningsResultAction;

/**
 * Provides a token that evaluates to the number of compiler warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class WarningsCountTokenMacro extends AbstractResultTokenMacro {
    /**
     * Creates a new instance of {@link WarningsCountTokenMacro}.
     */
    public WarningsCountTokenMacro() {
        super(WarningsResultAction.class, "WARNINGS_COUNT");
    }
}

