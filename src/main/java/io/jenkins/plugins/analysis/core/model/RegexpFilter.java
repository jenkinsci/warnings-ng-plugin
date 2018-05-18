package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Defines a filter criteria based on a regular expression for {@link Report}.
 *
 * @author Ulli Hafner
 */
public class RegexpFilter extends AbstractDescribableImpl<RegexpFilter> implements Serializable {
    private final String pattern;
    private final IssuesFilter property;

    /**
     * Creates a new instance of {@link RegexpFilter}.
     *
     * @param pattern
     *         the regular expression of the filter
     * @param property
     *         the property to filter by
     */
    @DataBoundConstructor
    public RegexpFilter(final String pattern, final IssuesFilter property) {
        super();

        this.pattern = pattern;
        this.property = property;
    }

    /**
     * Returns the regular expression of the filter.
     *
     * @return the regular expression of the filter
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the property that will be filtered by.
     *
     * @return the property to be filtered by
     */
    public IssuesFilter getProperty() {
        return property;
    }

    /**
     * Applies the filter on the specified builder.
     *
     * @param builder
     *         the issue filter builder
     */
    public void apply(final IssueFilterBuilder builder) {
        property.apply(builder, pattern);
    }

    /**
     * Dummy descriptor for {@link RegexpFilter}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<RegexpFilter> {
        // Required for Jenkins
    }
}