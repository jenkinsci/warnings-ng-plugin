package io.jenkins.plugins.analysis.core.model;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Issues.IssueFilterBuilder;

import hudson.Extension;

/**
 * Defines a filter criteria for {@link Issues}.
 *
 * @author Ulli Hafner
 */
public class IncludeModule extends IssuesFilter {
    /**
     * Creates a new instance of {@link IncludeModule}.
     */
    @DataBoundConstructor
    public IncludeModule() {
        // Required for Stapler
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String pattern) {
        builder.setIncludeModuleNameFilter(pattern);
    }

    /**
     * Dummy descriptor for {@link IncludeModule}.
     *
     * @author Ulli Hafner
     */
   @Extension
   public static class DescriptorImpl extends IncludeFilterDescriptor {
        // Required for Jenkins
   }
}