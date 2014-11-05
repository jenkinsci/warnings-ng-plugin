package hudson.plugins.warnings.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractFixedAnnotationsTokenMacro;
import hudson.plugins.warnings.AggregatedWarningsResultAction;

/**
 * Provides a token that evaluates to the number of fixed warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class FixedWarningsTokenMacro extends AbstractFixedAnnotationsTokenMacro {
    /**
     * Creates a new instance of {@link FixedWarningsTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public FixedWarningsTokenMacro() {
        super("WARNINGS_FIXED", AggregatedWarningsResultAction.class);
    }
}

