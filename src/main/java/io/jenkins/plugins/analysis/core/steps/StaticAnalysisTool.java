package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;

import org.apache.commons.text.WordUtils;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.util.NoSuchElementException;
import jenkins.model.Jenkins;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AnnotationParser;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisTool extends AbstractDescribableImpl<StaticAnalysisTool>
        implements AnnotationParser, ExtensionPoint {

    /**
     * Finds the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     *
     * @return the static analysis tool
     * @throws NoSuchElementException
     *         if the tool could not be found
     */
    public static StaticAnalysisLabelProvider find(final String id) {
        if (DefaultLabelProvider.STATIC_ANALYSIS_ID.equals(id)) {
            return new DefaultLabelProvider();
        }
        for (StaticAnalysisToolDescriptor toolDescriptor : all()) {
            if (toolDescriptor.getId().equals(id)) {
                return toolDescriptor.getLabelProvider();
            }
        }
        throw new NoSuchElementException("No static analysis tool found with ID %s.", id);
    }

    private static DescriptorExtensionList<StaticAnalysisTool, StaticAnalysisToolDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(StaticAnalysisTool.class);
    }

    private String defaultEncoding;

    @CheckForNull
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding used to read files (warnings, source code, etc.).
     *
     * @param defaultEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public String toString() {
        return String.format("[%s] Encoding: %s" , getName(), defaultEncoding);
    }

    /**
     * Returns the name of this tool.
     *
     * @return the nae of this tool
     */
    public String getName() {
        return getDescriptor().getDisplayName();
    }

    public String getId() {
        return getDescriptor().getId();
    }

    /** Descriptor for {@link StaticAnalysisTool}. **/
    public static abstract class StaticAnalysisToolDescriptor extends Descriptor<StaticAnalysisTool>
            implements StaticAnalysisLabelProvider {
        private final DefaultLabelProvider defaultLabelProvider = new DefaultLabelProvider();

        private final String id;

        /**
         * Creates a new {@link StaticAnalysisToolDescriptor} with the specified ID.
         *
         * @param id
         *         the ID
         */
        protected StaticAnalysisToolDescriptor(final String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        /**
         * Returns the associated label provider for this tool.
         *
         * @return the label provider
         */
        public StaticAnalysisLabelProvider getLabelProvider() {
            return this;
        }

        @Override
        public String getName() {
            return defaultLabelProvider.getName();
        }

        public String getSuffix() {
            return String.format(" (%s)", WordUtils.capitalize(getId()));
        }

        @Override
        public String getLinkName() {
            return defaultLabelProvider.getLinkName();
        }

        @Override
        public String getTrendName() {
            return defaultLabelProvider.getTrendName();
        }

        @Override
        public String getSmallIconUrl() {
            return defaultLabelProvider.getSmallIconUrl();
        }

        @Override
        public String getLargeIconUrl() {
            return defaultLabelProvider.getLargeIconUrl();
        }

        @Override
        public String getResultUrl() {
            return defaultLabelProvider.getResultUrl();
        }

        @Override
        public String getTooltip(final int numberOfItems) {
            return defaultLabelProvider.getTooltip(numberOfItems);
        }

        @Override
        public String getSummary(final int numberOfIssues, final int numberOfModules) {
            return defaultLabelProvider.getSummary(numberOfIssues, numberOfModules);
        }

        @Override
        public String getDeltaMessage(final int newSize, final int fixedSize) {
            return defaultLabelProvider.getDeltaMessage(newSize, fixedSize);
        }
    }
}
