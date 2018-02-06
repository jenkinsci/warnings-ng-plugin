package io.jenkins.plugins.analysis.core.model;

import javax.annotation.CheckForNull;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.util.NoSuchElementException;
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool.StaticAnalysisToolDescriptor;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;

/**
 * Creates {@link StaticAnalysisLabelProvider} instances based on a provided ID and name.
 *
 * @author Ullrich Hafner
 */
public class LabelProviderFactory {
    private final JenkinsFacade jenkins;

    /**
     * Creates a new instance of {@link ToolRegistry}.
     */
    public LabelProviderFactory() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    LabelProviderFactory(final JenkinsFacade jenkins) {
        this.jenkins = jenkins;
    }

    /**
     * Finds the label provider for the static analysis tool with the specified ID.
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
    public StaticAnalysisLabelProvider create(final String id, @CheckForNull final String name) {
        DescriptorExtensionList<StaticAnalysisTool, StaticAnalysisToolDescriptor> extensions
                = jenkins.getDescriptorsFor(StaticAnalysisTool.class);
        for (StaticAnalysisToolDescriptor descriptor : extensions) {
            if (descriptor.getId().equals(id)) {
                return wrapLabelProvider(descriptor.getLabelProvider(), name);
            }
        }

        List<StaticAnalysisToolFactory> factories = jenkins.getExtensionsFor(StaticAnalysisToolFactory.class);
        for (StaticAnalysisToolFactory factory : factories) {
            for (StaticAnalysisTool tool : factory.getTools()) {
                if (tool.getId().equals(id)) {
                    return wrapLabelProvider(tool.getLabelProvider(), name);
                }
            }
        }

        return new DefaultLabelProvider(id, name);
    }

    private StaticAnalysisLabelProvider wrapLabelProvider(final StaticAnalysisLabelProvider labelProvider, final String name) {
        if (StringUtils.isNotBlank(name)) {
            return new CompositeLabelProvider(labelProvider, name);
        }
        else {
            return labelProvider;
        }
    }

    /**
     * Provides additional {@link StaticAnalysisTool static analysis tool} instances that are created dynamically.
     */
    public interface StaticAnalysisToolFactory extends ExtensionPoint {
        /**
         * Returns the additional static analysis tools.
         *
         * @return the tools
         */
        List<StaticAnalysisTool> getTools();
    }
}
