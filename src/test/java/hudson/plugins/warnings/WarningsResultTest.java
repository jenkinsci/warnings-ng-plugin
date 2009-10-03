package hudson.plugins.warnings;

import static junit.framework.Assert.*;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.test.BuildResultTest;
import hudson.plugins.analysis.util.BuildResult;
import hudson.plugins.analysis.util.ParserResult;

/**
 * Tests the class {@link WarningsResult}.
 */
public class WarningsResultTest extends BuildResultTest<WarningsResult> {
    /** {@inheritDoc} */
    @Override
    protected WarningsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project) {
        return new WarningsResult(build, null, project);
    }

    /** {@inheritDoc} */
    @Override
    protected WarningsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final WarningsResult previous) {
        return new WarningsResult(build, null, project, previous);
    }

    /** {@inheritDoc} */
    @Override
    protected void verifyHighScoreMessage(final int expectedZeroWarningsBuildNumber, final boolean expectedIsNewHighScore, final long expectedHighScore, final long gap, final WarningsResult result) {
        if (result.hasNoAnnotations() && result.getDelta() == 0) {
            assertTrue(result.getDetails().contains(Messages.Warnings_ResultAction_NoWarningsSince(expectedZeroWarningsBuildNumber)));
            if (expectedIsNewHighScore) {
                long days = BuildResult.getDays(expectedHighScore);
                if (days == 1) {
                    assertTrue(result.getDetails().contains(Messages.Warnings_ResultAction_OneHighScore()));
                }
                else {
                    assertTrue(result.getDetails().contains(Messages.Warnings_ResultAction_MultipleHighScore(days)));
                }
            }
            else {
                long days = BuildResult.getDays(gap);
                if (days == 1) {
                    assertTrue(result.getDetails().contains(Messages.Warnings_ResultAction_OneNoHighScore()));
                }
                else {
                    assertTrue(result.getDetails().contains(Messages.Warnings_ResultAction_MultipleNoHighScore(days)));
                }
            }
        }
    }
}

