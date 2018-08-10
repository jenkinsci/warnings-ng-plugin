package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Empty build history.
 *
 * @author Ullrich Hafner
 */
public class NullAnalysisHistory extends AnalysisHistory {
    /**
     * Creates a new instance of {@link NullAnalysisHistory}.
     */
    public NullAnalysisHistory() {
        super(null, null);
    }

    @Override
    public Optional<AnalysisResult> getResult() {
        return Optional.empty();
    }
}

