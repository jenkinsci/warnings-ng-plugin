package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.AbstractAnnotationsBuildResultTest;
import hudson.plugins.warnings.util.ParserResult;

/**
 * Tests the class {@link WarningsResult}.
 */
public class WarningsResultTest extends AbstractAnnotationsBuildResultTest<WarningsResult> {
    /** {@inheritDoc} */
    @Override
    protected WarningsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project) {
        return new WarningsResult(build, project);
    }

    /** {@inheritDoc} */
    @Override
    protected WarningsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final WarningsResult previous) {
        return new WarningsResult(build, project, previous);
    }
}

