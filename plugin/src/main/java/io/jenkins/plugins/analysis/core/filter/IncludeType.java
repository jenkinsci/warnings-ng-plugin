package io.jenkins.plugins.analysis.core.filter;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Defines a filter criteria for a {@link Report}.
 *
 * @author Ullrich Hafner
 */
public class IncludeType extends RegexpFilter {
    private static final long serialVersionUID = -4251535355471690174L;

    /**
     * Creates a new instance of {@link IncludeType}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public IncludeType(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setIncludeTypeFilter(getPattern());
    }

    /**
     * Descriptor for {@link IncludeType}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    @Symbol("includeType")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Type();
        }
    }
}