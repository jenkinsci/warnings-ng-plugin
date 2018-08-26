package io.jenkins.plugins.analysis.core.filter;

import java.io.Serializable;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Defines a filter criteria based on a regular expression for {@link Report}.
 *
 * @author Ullrich Hafner
 */
public abstract class RegexpFilter extends AbstractDescribableImpl<RegexpFilter> implements Serializable {
    private static final long serialVersionUID = 1892735849628260157L;

    private final String pattern;

    /**
     * Creates a new instance of {@link RegexpFilter}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    public RegexpFilter(final String pattern) {
        super();

        this.pattern = pattern;
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
     * Applies the filter on the specified builder.
     *
     * @param builder
     *         the issue filter builder
     */
    public abstract void apply(IssueFilterBuilder builder);

    @Override
    public RegexpFilterDescriptor getDescriptor() {
        return (RegexpFilterDescriptor) super.getDescriptor();
    }

    /** 
     * Dummy descriptor for {@link RegexpFilter}.
     */
    public abstract static class RegexpFilterDescriptor extends Descriptor<RegexpFilter> {

    }
}