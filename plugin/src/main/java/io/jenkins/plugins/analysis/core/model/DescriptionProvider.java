package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;

/**
 * Provides an additional description for an issue.
 *
 * @author Ullrich Hafner
 */
public interface DescriptionProvider {
    /**
     * Returns a detailed description of the specified issue.
     *
     * @param issue
     *         the issue to get the description for
     *
     * @return the description
     */
    String getDescription(Issue issue);
}
