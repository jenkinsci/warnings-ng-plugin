package io.jenkins.plugins.analysis.core.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;

import io.jenkins.plugins.analysis.core.model.Tool.ToolDescriptor;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

/**
 * Creates {@link StaticAnalysisLabelProvider} instances based on a provided ID and name.
 *
 * @author Ullrich Hafner
 */
public class LabelProviderFactory {
    private final JenkinsFacade jenkins;

    /**
     * Creates a new instance of {@link LabelProviderFactory}.
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
     *
     * @return The label provider of the selected static analysis tool. If the tool is not found then a default label
     *         provider is returned.
     */
    public StaticAnalysisLabelProvider create(final String id) {
        return create(id, StringUtils.EMPTY);
    }

    /**
     * Finds the label provider for the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     * @param name
     *         the name of the tool (might be empty or null)
     *
     * @return The label provider of the selected static analysis tool. If the tool is not found then a default label
     *         provider is returned.
     */
    public StaticAnalysisLabelProvider create(final String id, @Nullable final String name) {
        DescriptorExtensionList<Tool, ToolDescriptor> extensions
                = jenkins.getDescriptorsFor(Tool.class);
        for (ToolDescriptor descriptor : extensions) {
            if (descriptor.getId().equals(id)) {
                return createNamedLabelProvider(descriptor.getLabelProvider(), name);
            }
        }

        List<StaticAnalysisToolFactory> factories = jenkins.getExtensionsFor(StaticAnalysisToolFactory.class);
        for (StaticAnalysisToolFactory factory : factories) {
            for (Tool tool : factory.getTools()) {
                if (tool.getActualId().equals(id)) {
                    return createNamedLabelProvider(tool.getLabelProvider(), name);
                }
            }
        }

        return new StaticAnalysisLabelProvider(id, name);
    }

    private StaticAnalysisLabelProvider createNamedLabelProvider(
            final StaticAnalysisLabelProvider labelProvider, final String name) {
        labelProvider.setName(name);
        return labelProvider;
    }

    /**
     * Provides additional {@link Tool static analysis tool} instances that are created dynamically.
     */
    public interface StaticAnalysisToolFactory extends ExtensionPoint {
        /**
         * Returns the additional static analysis tools.
         *
         * @return the tools
         */
        List<Tool> getTools();
    }
}
