package io.jenkins.plugins.analysis.core.views;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.steps.Messages;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Result object representing a dynamic tab.
 *
 * @author Ulli Hafner
 */
public class TabDetail extends IssuesDetail {
    /** URL of the content to load. */
    private final String url;
    private final Map<String, Long> propertyCount;
    private final Function<String, String> propertyFormatter;

    /**
     * Creates a new instance of {@link TabDetail}.
     *
     * @param propertySelector
     * @param owner
     *         current build as owner of this action.
     * @param issues
     *         the module to show the details for
     * @param url
     *         URL to render the content of this tab
     */
    public TabDetail(final Run<?, ?> owner, final Issues issues, final String url, final String defaultEncoding, final ModelObject parent,
            final Function<Issue, String> propertySelector, final Function<String, String> propertyFormatter) {
        super(owner, issues, new Issues(), new Issues(), defaultEncoding, parent, Messages._Default_Name());

        this.url = url;
        propertyCount = getIssues().getPropertyCount(propertySelector);
        this.propertyFormatter = propertyFormatter;
    }

    /**
     * Returns the URL that renders the content of this tab.
     *
     * @return the URL
     */
    @Override
    public String getUrl() {
        return url;
    }

    public long getMax() {
        return Collections.max(propertyCount.values());
    }

    public String getDisplayName(final String key) {
        return propertyFormatter.apply(key);
    }

    public Set<String> getKeys() {
        return propertyCount.keySet();
    }

    public long getCount(final String key) {
        return propertyCount.get(key);
    }
}

