package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

import edu.hm.hafner.analysis.Issues.IssueFilterBuilder;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public abstract class IncludeFilter extends AbstractDescribableImpl<IncludeFilter> implements Serializable {
    private final String name;

    public IncludeFilter(final String name) {
        super();
        this.name = name;
    }

    /**
     * Returns the regular expression of the filter.
     *
     * @return the regular expression of the filter
     */
    public String getName() {
        return name;
    }

    public abstract void apply(final IssueFilterBuilder builder, final String regexp);

    /**
     * Dummy descriptor for {@link IncludeFilter}.
     *
     * @author Ulli Hafner
     */
    public static class IncludeFilterDescriptor extends Descriptor<IncludeFilter> {
        // Required for Jenkins
    }

}
