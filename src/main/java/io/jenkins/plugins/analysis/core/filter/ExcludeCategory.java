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
public class ExcludeCategory extends RegexpFilter {
    private static final long serialVersionUID = 8704648332922985878L;

    /**
     * Creates a new instance of {@link ExcludeCategory}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public ExcludeCategory(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setExcludeCategoryFilter(getPattern());
    }

    /**
     * Descriptor for {@link ExcludeCategory}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    @Symbol("excludeCategory")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Category();
        }
    }
}