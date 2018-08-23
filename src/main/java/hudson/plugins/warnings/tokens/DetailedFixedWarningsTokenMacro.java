package hudson.plugins.warnings.tokens;

import hudson.Extension;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.warnings.AggregatedWarningsResultAction;

/**
 * Provides a token that contain details of fixed warnings.
 *
 * @author Benedikt Spranger
 * @deprecated replaced by classes of io.jenkins.plugins.analysis package
 */
@Deprecated
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
