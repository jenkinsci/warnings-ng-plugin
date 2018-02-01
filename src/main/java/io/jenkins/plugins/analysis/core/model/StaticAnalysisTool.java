package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.IssueParser;

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
    public abstract IssueParser<?> createParser();

}
