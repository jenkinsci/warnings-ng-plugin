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
    @Extension
    @Symbol("excludeModule")
    public static class DescriptorImpl extends RegexpFilterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Filter_Exclude_Module();
        }
    }
}