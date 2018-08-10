package hudson.plugins.warnings.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractNewAnnotationsTokenMacro;
import hudson.plugins.warnings.AggregatedWarningsResultAction;

/**
 * Provides a token that evaluates to the number of new warnings.
 *
 * @author Ullrich Hafner
 */
@Extension(optional = true)
public class NewWarningsTokenMacro extends AbstractNewAnnotationsTokenMacro {
    /**
     * Creates a new instance of {@link NewWarningsTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public NewWarningsTokenMacro() {
        super("WARNINGS_NEW", AggregatedWarningsResultAction.class);
    }
}

