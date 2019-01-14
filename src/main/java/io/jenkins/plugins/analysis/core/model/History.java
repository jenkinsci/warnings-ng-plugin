package io.jenkins.plugins.analysis.core.model;

import java.util.Iterator;
import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.model.Run;

public interface History extends Iterable<AnalysisResult> {
    Optional<ResultAction> getBaselineAction();

    Optional<AnalysisResult> getBaselineResult();

    Optional<AnalysisResult> getResult();

    Optional<Run<?, ?>> getBuild();

    Report getIssues();

    @Override
    @NonNull
    Iterator<AnalysisResult> iterator();
}
