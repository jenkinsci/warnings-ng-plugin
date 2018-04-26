package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import hudson.model.Run;
import hudson.plugins.analysis.core.IssueDifference;

import edu.hm.hafner.analysis.Issues;

/**
 * Provides the reference result for a new static analysis run. When outstanding, new, and fixed issues are computed
 * (see {@link IssueDifference}) for a new static analysis run then an instance of this reference is used as a
 * baseline.
 *
 * @author Ullrich Hafner
 */
public interface ReferenceProvider {
    /**
     * Returns the issues of the reference run. If there is no reference, then an empty set of issues is returned.
     *
     * @return the issues of the reference run
     */
    Issues getIssues();

    /**
     * Returns the actual reference run.
     *
     * @return the {@link Run} that is used as reference
     */
    Optional<Run<?, ?>> getAnalysisRun();
}
