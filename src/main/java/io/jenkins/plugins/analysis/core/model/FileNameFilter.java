package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Issues.IssueFilterBuilder;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Defines a filter criteria for {@link Issues}.
 *
 * @author Ulli Hafner
 */
public class FileNameFilter extends AbstractDescribableImpl<FileNameFilter> implements Serializable {
    private final String regexp;
    private final IncludeFilter property;

    /**
     * Creates a new instance of {@link FileNameFilter}.
     *
     * @param regexp
     *            the regular expression of the filter
     */
    @DataBoundConstructor
    public FileNameFilter(final String regexp, final IncludeFilter property) {
        super();

        this.regexp = regexp;
        this.property = property;
    }

    /**
     * Returns the regular expression of the filter
     *
     * @return the regular expression of the filter
     */
    public String getRegexp() {
        return regexp;
    }

    public IncludeFilter getProperty() {
        return property;
    }

    public void apply(final IssueFilterBuilder builder) {
        property.apply(builder, regexp);
    }

    /**
     * Dummy descriptor for {@link FileNameFilter}.
     *
     * @author Ulli Hafner
     */
   @Extension
   public static class DescriptorImpl extends Descriptor<FileNameFilter> {
        // Required for Jenkins
   }
}