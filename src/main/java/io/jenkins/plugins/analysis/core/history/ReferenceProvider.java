package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;

import hudson.model.Run;
import hudson.plugins.analysis.core.IssueDifference;

/**
 * Provides a reference build that contains static analysis results. When outstanding, new, and fixed issues are
 * computed (see {@link IssueDifference}) then an instance of this reference is used as a baseline. The results of the
 * current build are then compared with the results of this reference.
 *
 * @author Ullrich Hafner
 */
public interface ReferenceProvider {
    /**
     * Returns the issues of the reference. If there is no reference, then an empty set of issues is returned.
     *
     * @return the issues of the reference
     */
    Report getIssues();

    /**
     * Returns the actual reference build. This build is guaranteed to contain an {@link AnalysisResult} of the
     * specified type.
     *
     * @return the {@link Run} that can be used as reference
     */
    Optional<Run<?, ?>> getBuild();
}
