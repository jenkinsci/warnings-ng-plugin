package io.jenkins.plugins.analysis.core.model;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.util.NoSuchElementException;

/**
 * Creates {@link StaticAnalysisLabelProvider} instances based on a provided ID and name.
 *
 * @author Ullrich Hafner
 */
public class LabelProviderFactory {
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
    public StaticAnalysisLabelProvider findLabelProvider(final String id, @CheckForNull final String name) {
        ToolRegistry registry = new ToolRegistry();
        if (registry.contains(id)) {
            StaticAnalysisLabelProvider labelProvider = registry.find(id).getLabelProvider();
            if (StringUtils.isNotBlank(name)) {
                return new CompositeLabelProvider(labelProvider, name);
            }
            else {
                return labelProvider;
            }
        }
        return new DefaultLabelProvider(id, name);
    }
}
