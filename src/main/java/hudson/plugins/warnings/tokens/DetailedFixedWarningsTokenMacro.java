package hudson.plugins.warnings.tokens;

import hudson.Extension;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.AggregatedWarningsResultAction;

/**
 * Provides a token that contain details of fixed warnings.
 *
 * @author Benedikt Spranger
 */
@Extension(optional = true)
public class DetailedFixedWarningsTokenMacro extends AbstractDetailedTokenMacro {
    /**
     * Creates a new instance of {@link DetailedFixedWarningsTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public DetailedFixedWarningsTokenMacro() {
        super("WARNINGS_FIXED_DETAILED", AggregatedWarningsResultAction.class);
    }

    @Override
    protected String evaluate(final BuildResult result) {
        return evalWarnings(result, result.getFixedWarnings()).replace("<br>", "\n");
    }
}
