package hudson.plugins.analysis.util;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.util.NoSuchElementException;

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
    boolean hasPreviousResultAction();

    /**
     * Returns the result action from the previous build.
     *
     * @return the result of the previous build.
     * @throws NoSuchElementException if there is no previous result action is found
     */
    ResultAction<T> getPreviousResultAction();

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
}
