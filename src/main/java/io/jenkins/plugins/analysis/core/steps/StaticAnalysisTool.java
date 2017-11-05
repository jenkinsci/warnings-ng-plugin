package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.util.NoSuchElementException;
import jenkins.model.Jenkins;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisTool extends AbstractDescribableImpl<StaticAnalysisTool>
        implements IssueParser, ExtensionPoint {

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
    private static StaticAnalysisLabelProvider find(final String id) {
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

    /**
     * Finds the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     * @param name
     *         the name of the tool (might be empty or null)
     *
     * @return the static analysis tool
     * @throws NoSuchElementException
     *         if the tool could not be found
     */
    public static StaticAnalysisLabelProvider find(final String id, @CheckForNull final String name) {
        if (StringUtils.isBlank(name)) {
            return find(id);
        }
        else {
            return new DefaultLabelProvider(id, name);
        }
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
        return String.format("[%s] Encoding: %s", getName(), defaultEncoding);
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

    protected Issues<Issue> withOrigin(final Issues<Issue> issues, final String origin) {
        IssueBuilder builder = new IssueBuilder();
        Issues<Issue> issuesWithOrigin = new Issues<>();
        for (Issue noOrigin : issues.all()) {
            issuesWithOrigin.add(builder.copy(noOrigin).setOrigin(origin).build());
        }
        return issuesWithOrigin;
    }

    /** Descriptor for {@link StaticAnalysisTool}. **/
    public abstract static class StaticAnalysisToolDescriptor extends Descriptor<StaticAnalysisTool> {
        private final StaticAnalysisLabelProvider labelProvider;

        /**
         * Creates a new {@link StaticAnalysisToolDescriptor} with the specified label provider.
         *
         * @param labelProvider
         *         the label provider to use
         */
        protected StaticAnalysisToolDescriptor(final StaticAnalysisLabelProvider labelProvider) {
            this.labelProvider = labelProvider;
        }

        @Override
        public String getId() {
            return labelProvider.getId();
        }

        /**
         * Returns the associated label provider for this tool.
         *
         * @return the label provider
         */
        public StaticAnalysisLabelProvider getLabelProvider() {
            return labelProvider;
        }
    }
}
