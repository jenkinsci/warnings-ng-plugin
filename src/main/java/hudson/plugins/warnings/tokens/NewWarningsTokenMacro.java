package hudson.plugins.warnings.tokens;

import hudson.Extension;

import hudson.plugins.analysis.tokens.AbstractNewAnnotationsTokenMacro;
import hudson.plugins.warnings.WarningsResultAction;

/**
 * Provides a token that evaluates to the number of new warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class NewWarningsTokenMacro extends AbstractNewAnnotationsTokenMacro {
    /**
     * Creates a new instance of {@link NewWarningsTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public NewWarningsTokenMacro() {
        super("WARNINGS_NEW", WarningsResultAction.class);
    }
}

