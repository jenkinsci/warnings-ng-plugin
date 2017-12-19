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
public class IncludeFile extends IncludeFilter {
    /**
     * Creates a new instance of {@link IncludeFile}.
     *
     * @param name
     *            the regular expression of the filter
     */
    @DataBoundConstructor
    public IncludeFile(final String name) {
        super(name);
    }

    @Override
    public void apply(final IssueFilterBuilder builder, final String regexp) {
        builder.setIncludeFilenameFilter(regexp);
    }

    /**
     * Dummy descriptor for {@link IncludeFile}.
     *
     * @author Ulli Hafner
     */
   @Extension
   public static class DescriptorImpl extends IncludeFilterDescriptor {
        // Required for Jenkins
   }
}