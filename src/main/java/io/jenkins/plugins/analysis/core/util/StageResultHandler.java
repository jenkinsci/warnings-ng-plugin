package io.jenkins.plugins.analysis.core.util;

import hudson.model.Result;

/**
 * Handles the setting of the results of a stage.
 */
public interface StageResultHandler {
    /**
     * Called to set the {@link Result} of a stage.
     *
     * @param result
     *         the result to set
     * @param message
     *         a message that describes the cause for the result
     */
    void setResult(Result result, String message);
}
