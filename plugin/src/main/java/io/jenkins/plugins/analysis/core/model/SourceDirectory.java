package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Directory that contains the source files that have issues.
 *
 * @author Ullrich Hafner
 */
public class SourceDirectory extends AbstractDescribableImpl<SourceDirectory> implements Serializable {
    private static final long serialVersionUID = -3864564528382064924L;

    private final String path;

    /**
     * Creates a new instance of {@link SourceDirectory}.
     *
     * @param path
     *         the name of the folder
     */
    @DataBoundConstructor
    public SourceDirectory(final String path) {
        super();

        this.path = path;
    }

    public String getPath() {
        return path;
    }

    /**
     * Descriptor to validate {@link SourceDirectory}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<SourceDirectory> {
        @NonNull
        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }
    }
}

