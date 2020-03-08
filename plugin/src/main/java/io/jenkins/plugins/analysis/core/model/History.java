package io.jenkins.plugins.analysis.core.model;

import java.util.Iterator;
import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.echarts.BuildResult;
import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * History of analysis results.
 */
public interface History extends Iterable<BuildResult<AnalysisBuildResult>> {
    /**
     * Returns the baseline action (if already available).
     *
     * @return the baseline action
     */
    Optional<ResultAction> getBaselineAction();

    /**
     * Returns the baseline result (if already available).
     *
     * @return the baseline result
     */
    Optional<AnalysisResult> getBaselineResult();

    /**
     * Returns the historical result (if there is any).
     *
     * @return the historical result
     */
    Optional<AnalysisResult> getResult();

    /**
     * Returns the build that contains the historical result (if there is any).
     *
     * @return the historical result
     */
    Optional<Run<?, ?>> getBuild();

    /**
     * Returns the issues of the historical result. If there is no historical build found, then an empty set of issues
     * is returned.
     *
     * @return the issues of the historical build
     */
    Report getIssues();

    @Override
    @NonNull
    Iterator<BuildResult<AnalysisBuildResult>> iterator();

    /**
     * Returns whether this history has more than one result.
     *
     * @return {@code true} if there are multiple results, {@code false} otherwise
     */
    boolean hasMultipleResults();
}
