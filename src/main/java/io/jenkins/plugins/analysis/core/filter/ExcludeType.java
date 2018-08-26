package io.jenkins.plugins.analysis.core.filter;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.model.Messages;

import hudson.Extension;
import hudson.model.Descriptor;

/**
 * Defines a filter criteria for a {@link Report}.
 *
 * @author Ullrich Hafner
 */
public class ExcludeType extends RegexpFilter {
    private static final long serialVersionUID = -9215604002784734848L;

    /**
     * Creates a new instance of {@link ExcludeType}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public ExcludeType(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setExcludeTypeFilter(getPattern());
    }

    /**
     * Descriptor for {@link ExcludeType}.
     *
     * @author Ullrich Hafner
     */
    @Extension @Symbol("excludeType")
    public static class DescriptorImpl extends Descriptor<RegexpFilter> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Type();
        }
    }
}