package io.jenkins.plugins.analysis.core.model;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.util.NoSuchElementException;

import hudson.ExtensionPoint;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisTool implements ExtensionPoint {
    /**
     * Returns the ID of this tool.
     *
     * @return the label provider
     */
    public String getId() {
        return getLabelProvider().getId();
    }

    /**
     * Returns a human readable name for this tool.
     *
     * @return the label provider
     */
    public String getName() {
        return getLabelProvider().getName();
    }

    /**
     * Returns the associated label provider for this tool.
     *
     * @return the label provider
     */
    public abstract StaticAnalysisLabelProvider getLabelProvider();

    /**
     * Returns a new parser to scan a log file and return the issues reported in such a file.
     *
     * @return the parser to use
     */
    public abstract IssueParser createParser();

    /**
     * Finds the label provider for the static analysis tool with the specified ID.
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
        ToolRegistry registry = new ToolRegistry();
        return registry.find(id).getLabelProvider();
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
    public static StaticAnalysisLabelProvider find(final String id, @CheckForNull final String name) {
        if (StringUtils.isBlank(name)) {
            return find(id);
        }
        else {
            return new DefaultLabelProvider(id, name);
        }
    }
}
