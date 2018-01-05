package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Empty build history.
 *
 * @author Ulli Hafner
 */
// FIXME: still required?
public class NullBuildHistory extends BuildHistory {
    /**
     * Creates a new instance of {@link NullBuildHistory}.
     */
    public NullBuildHistory() {
        super(null, null);
    }

    @Override
    public Optional<AnalysisResult> getPreviousResult() {
        return Optional.empty();
    }
}

