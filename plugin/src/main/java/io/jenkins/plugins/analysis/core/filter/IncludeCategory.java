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
public class IncludeCategory extends RegexpFilter {
    private static final long serialVersionUID = -3109697929021646731L;

    /**
     * Creates a new instance of {@link IncludeCategory}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public IncludeCategory(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setIncludeCategoryFilter(getPattern());
    }

    /**
     * Descriptor for {@link IncludeCategory}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    @Symbol("includeCategory")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Include_Category();
        }
    }
}