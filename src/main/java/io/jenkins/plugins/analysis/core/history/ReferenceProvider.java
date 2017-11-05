package io.jenkins.plugins.analysis.core.history;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.steps.BuildIssue;

import hudson.model.Run;
import hudson.plugins.analysis.core.IssueDifference;

/**
 * Provides the reference result for a new static analysis run. When old, new, and fixed issues are computed (see {@link
 * IssueDifference}) for a new static analysis run then an instance of this reference is used as a baseline.
 *
 * @author Ullrich Hafner
 */
public interface ReferenceProvider {
    /** Indicates that no reference has been found. */
    int NO_REFERENCE_FOUND = -1;

    /**
     * Returns the issues of the reference build.
     *
     * @return the issues of the reference build
     */
    Issues<BuildIssue> getIssues();

    /**
     * Returns the number of the reference run.
     *
     * @return the number of the {@link Run} that is used as reference, or {@link #NO_REFERENCE_FOUND}
     * if no such run exists.
     */
    int getNumber();
}
