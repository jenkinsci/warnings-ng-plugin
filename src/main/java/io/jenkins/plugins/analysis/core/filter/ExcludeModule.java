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
public class ExcludeModule extends RegexpFilter {
    private static final long serialVersionUID = 8640962711241699659L;

    /**
     * Creates a new instance of {@link ExcludeModule}.
     *
     * @param pattern
     *         the regular expression of the filter
     */
    @DataBoundConstructor
    public ExcludeModule(final String pattern) {
        super(pattern);
    }

    @Override
    public void apply(final IssueFilterBuilder builder) {
        builder.setExcludeModuleNameFilter(getPattern());
    }

    /**
     * Descriptor for {@link ExcludeModule}.
     *
     * @author Ullrich Hafner
     */
    @Extension @Symbol("excludeModule")
    public static class DescriptorImpl extends Descriptor<RegexpFilter> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Module();
        }
    }
}