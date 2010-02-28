package hudson.plugins.analysis.core;

import hudson.model.AbstractBuild;
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
     * Returns whether a previous build already has a result action of this type
     * attached.
     *
     * @return <code>true</code> a previous build already has a result action
     *         of this type attached
     */
    boolean hasPreviousAction();

    /**
     * Returns the result action from a previous build or <code>null</code> if
     * no such build is found.
     *
     * @return the result of the previous build, or <code>null</code>.
     */
    ResultAction<T> getPreviousAction();

    /**
     * Returns whether a previous build already has a successful result action
     * of this type attached.
     *
     * @return <code>true</code> a previous build already has a result action of
     *         this type attached
     */
    boolean hasReferenceAction();

    /**
     * Returns the successful result action from a previous build or
     * <code>null</code> if no such build is found.
     *
     * @return the result of the previous build, or <code>null</code>.
     */
    ResultAction<T> getReferenceAction();

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    AbstractBuild<?, ?> getBuild();

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
