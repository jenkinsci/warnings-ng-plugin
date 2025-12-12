package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;

/**
 * Provides an additional description for an issue.
 *
 * @author Ullrich Hafner
 */
@FunctionalInterface
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

    /**
     * Returns an optional URL to the documentation for the given issue's category. If there is no URL available, then
     * an empty String is returned.
     *
     * @param issue
     *         the issue to get the category URL for
     *
     * @return the category documentation URL or an empty string
     */
    default String getCategoryUrl(final Issue issue) {
        return "";
    }
}
