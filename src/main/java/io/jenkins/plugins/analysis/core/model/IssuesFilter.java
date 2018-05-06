package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Base class for include filters for {@link Report} that use a regular expression to filter the issues for a specific
 * property.
 *
 * @author Ullrich Hafner
 */
public abstract class IssuesFilter extends AbstractDescribableImpl<IssuesFilter> implements Serializable {
    /**
     * Applies the filter on the specified builder using the specified regular expression.
     *
     * @param builder
     *         the builder to be changed
     * @param pattern
     *         the regular expression pattern for the filter
     */
    public abstract void apply(IssueFilterBuilder builder, String pattern);

    /**
     * Base class of a descriptor for an {@link IssuesFilter}.
     *
     * @author Ulli Hafner
     */
    public static class IncludeFilterDescriptor extends Descriptor<IssuesFilter> {
        // Required for Jenkins
    }
}
