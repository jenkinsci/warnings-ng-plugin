package io.jenkins.plugins.analysis.core.model;

import java.util.Optional;

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
    } // FIXME: does not work anymore

    @Override
    public Optional<AnalysisResult> getResult() {
        return Optional.empty();
    }
}

