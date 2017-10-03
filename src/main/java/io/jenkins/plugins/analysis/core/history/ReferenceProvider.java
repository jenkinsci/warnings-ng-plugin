package io.jenkins.plugins.analysis.core.history;

import javax.annotation.CheckForNull;

import hudson.model.Run;
import hudson.plugins.analysis.core.IssueDifference;
import hudson.plugins.analysis.util.model.AnnotationContainer;

/**
 * Provides the reference result for a new static analysis run. When old, new, and fixed issues are computed (see {@link
 * IssueDifference}) for a new static analysis run then an instance of this reference is used as a baseline.
 *
 * @author Ullrich Hafner
 */
public interface ReferenceProvider {
    /**
     * Returns the reference build or {@code null} if there is no such build.
     *
     * @return the reference build
     * @see #hasReference()
     */
    @CheckForNull
    Run<?, ?> getReference();

    /**
     * Returns whether a reference build is available to get results from.
     *
     * @return {@code true} if a reference build exists, {@code false} otherwise
     * @see #getReference()
     */
    boolean hasReference();

    /**
     * Returns the issues of the reference build.
     *
     * @return the issues of the reference build
     */
    AnnotationContainer getIssues();
}
