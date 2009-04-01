package hudson.plugins.warnings.util;

import hudson.model.Action;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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
     * Generates a PNG image showing the trend graph for this result action.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param height
     *            the height of the trend graph
     * @throws IOException
     *             in case of an error
     */
    void doGraph(StaplerRequest request, StaplerResponse response, int height) throws IOException;

    /**
     * Generates a clickable map for the trend graph of this result action.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param height
     *            the height of the trend graph
     * @throws IOException
     *             in case of an error
     */
    void doGraphMap(StaplerRequest request, StaplerResponse response, int height) throws IOException;

    /**
     * Returns the associated health report builder.
     *
     * @return the associated health report builder
     */
    HealthReportBuilder getHealthReportBuilder();
}
