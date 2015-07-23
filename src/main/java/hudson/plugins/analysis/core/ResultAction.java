package hudson.plugins.analysis.core;

import hudson.model.Run;
import hudson.model.Action;
import hudson.model.Result;

import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Defines an action that is responsible for handling results of the given type
 * <code>T</code>.
 *
 * @param <T>
 *            type of the result
 * @author Ulli Hafner
 */
public interface ResultAction<T extends BuildResult> extends Action {
    /**
     * Returns the current result of this action.
     *
     * @return the current result
     */
    T getResult();

    /**
     * Sets the result for this build.
     *
     * @param result the result to set
     */
    void setResult(final T result);

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    Run<?, ?> getBuild();

    /**
     * Returns the associated tool tip provider.
     *
     * @return the tool tip provider
     */
    ToolTipProvider getToolTipProvider();

    /**
     * Gets the associated health descriptor.
     *
     * @return the health descriptor
     */
    AbstractHealthDescriptor getHealthDescriptor();

    /**
     * Returns whether this build is successful with respect to the
     * {@link HealthDescriptor} of this action.
     *
     * @return <code>true</code> if the build is successful, <code>false</code>
     *         if the build has been set to {@link Result#UNSTABLE} or
     *         {@link Result#FAILURE} by this action.
     */
    boolean isSuccessful();
}
