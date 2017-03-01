package hudson.plugins.warnings.tokens;

import hudson.Extension;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.AggregatedWarningsResultAction;

/**
 * Provides a token that contain details of warnings.
 *
 * @author Benedikt Spranger
 */
@Extension(optional = true)
public class DetailedWarningsTokenMacro extends AbstractDetailedTokenMacro {
    /**
     * Creates a new instance of {@link DetailedWarningsTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public DetailedWarningsTokenMacro() {
        super("WARNINGS_DETAILED", AggregatedWarningsResultAction.class);
    }

    @Override
    protected String evaluate(final BuildResult result) {
        return evalWarnings(result, result.getAnnotations());
    }
}
