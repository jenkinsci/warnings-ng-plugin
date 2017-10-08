package io.jenkins.plugins.analysis.core.steps;

import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;

import hudson.model.Result;

/**
 * FIXME: remove this interface after testing has been finished.
 *
 * @author Ullrich Hafner
 */
public interface StaticAnalysisRun2 extends StaticAnalysisRun {
    int getZeroWarningsSinceBuild();

    boolean isSuccessfulTouched();

    boolean isSuccessful();

    String getReason();

    int getReferenceBuild();

    boolean isNewZeroWarningsHighScore();

    long getZeroWarningsHighScore();

    long getHighScoreGap();

    boolean isNewSuccessfulHighScore();

    long getSuccessfulHighScore();

    long getSuccessfulHighScoreGap();

    int getFixedSize();

    Result getPluginResult();
}
