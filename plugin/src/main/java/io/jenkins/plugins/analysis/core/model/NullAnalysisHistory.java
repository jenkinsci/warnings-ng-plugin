package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.echarts.BuildResult;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * Empty build history.
 *
 * @author Ullrich Hafner
 */
public class NullAnalysisHistory implements History {
    @Override
    public Optional<ResultAction> getBaselineAction() {
        return Optional.empty();
    }

    @Override
    public Optional<AnalysisResult> getBaselineResult() {
        return Optional.empty();
    }

    @Override
    public Optional<AnalysisResult> getResult() {
        return Optional.empty();
    }

    @Override
    public Optional<Run<?, ?>> getBuild() {
        return Optional.empty();
    }

    @Override
    public Report getIssues() {
        return new Report();
    }

    @NonNull
    @Override
    public Iterator<BuildResult<AnalysisBuildResult>> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean hasMultipleResults() {
        return false;
    }

    @Override
    public String toString() {
        return "No history found";
    }
}
