package io.jenkins.plugins.analysis.core.model;

import java.util.Iterator;
import java.util.Optional;
import javax.annotation.Nonnull;

import edu.hm.hafner.analysis.Report;

import hudson.model.Run;

public interface History extends Iterable<AnalysisResult> {
    Optional<ResultAction> getBaselineAction();

    Optional<AnalysisResult> getBaselineResult();

    Optional<AnalysisResult> getResult();

    Optional<Run<?, ?>> getBuild();

    Report getIssues();

    @Override
    @Nonnull
    Iterator<AnalysisResult> iterator();
}
