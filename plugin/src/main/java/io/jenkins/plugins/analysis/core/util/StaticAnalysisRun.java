package io.jenkins.plugins.analysis.core.util;

import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import hudson.model.Run;

import io.jenkins.plugins.util.QualityGateResult;
import io.jenkins.plugins.util.QualityGateStatus;

/**
 * Provides detailed information for the results of a static analysis run.
 */
public interface StaticAnalysisRun extends AnalysisBuildResult {
    /**
     * Returns the ID of the static analysis result.
     *
     * @return the ID
     */
    @Whitelisted
    String getId();

    /**
     * Returns the run that created this static analysis result.
     *
     * @return the run
     */
    Run<?, ?> getOwner();

    /**
     * Returns the error messages of the analysis run.
     *
     * @return the error messages
     */
    ImmutableList<String> getErrorMessages();

    /**
     * Returns the info messages of the analysis run.
     *
     * @return the info messages
     */
    ImmutableList<String> getInfoMessages();

    /**
     * Returns the build number since the associated job has a successful static analysis result.
     *
     * @return the build number since the static analysis result is successful, or -1 if the result is not successful
     */
    int getSuccessfulSinceBuild();

    /**
     * Returns the {@link QualityGateStatus} of the quality gates evaluation of the static analysis run.
     *
     * @return the quality gate status
     */
    QualityGateStatus getQualityGateStatus();

    /**
     * Returns the {@link QualityGateResult} of the quality gates evaluation of the static analysis run.
     *
     * @return the quality gate status
     */
    @Whitelisted
    QualityGateResult getQualityGateResult();

    /**
     * Returns the reference static analysis run that has been used to compute the new issues.
     *
     * @return the reference build
     */
    Optional<Run<?, ?>> getReferenceBuild();

    /**
     * Returns the build number since the associated job has no issues.
     *
     * @return the build number since there are no issues, or -1 if issues have been reported
     */
    int getNoIssuesSinceBuild();
}
