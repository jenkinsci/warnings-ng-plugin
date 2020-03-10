package io.jenkins.plugins.analysis.core.util;

import hudson.model.Result;
import hudson.model.Run;

/**
 * {@link StageResultHandler} that sets the overall build result of the {@link Run}.
 */
public class RunResultHandler implements StageResultHandler {
    private final Run<?, ?> run;

    /**
     * Creates a new instance of {@link RunResultHandler}.
     *
     * @param run
     *         the run to set the result for
     */
    public RunResultHandler(final Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void setResult(final Result result, final String message) {
        run.setResult(result);
    }
}
