package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueParser;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisTool extends AbstractDescribableImpl<StaticAnalysisTool>
        implements Serializable {
    /**
     * Returns the ID of this tool.
     *
     * @return the label provider
     */
    public String getId() {
        return getDescriptor().getId();
    }

    /**
     * Returns a human readable name for this tool.
     *
     * @return the label provider
     */
    public String getName() {
        return getDescriptor().getDisplayName();
    }

    /**
     * Returns the associated label provider for this tool.
     *
     * @return the label provider
     */
    public StaticAnalysisLabelProvider getLabelProvider() {
        return getDescriptor().getLabelProvider();
    }

    @Override
    public StaticAnalysisToolDescriptor getDescriptor() {
        return (StaticAnalysisToolDescriptor) super.getDescriptor();
    }

    /**
     * Returns a new parser to scan a log file and return the issues reported in such a file.
     *
     * @return the parser to use
     */
    public abstract IssueParser<?> createParser();

    /**
     * Returns whether this parser can scan the console log. Typically, only line based parsers can scan the console
     * log. XML parsers should always parse a given file only.
     *
     * @return the parser to use
     */
    public boolean canScanConsoleLog() {
        return true;
    }

    /** Descriptor for {@link StaticAnalysisTool}. **/
    public abstract static class StaticAnalysisToolDescriptor extends Descriptor<StaticAnalysisTool> {
        private final String id;

        /**
         * Creates a new instance of {@link StaticAnalysisToolDescriptor} with the given ID.
         *
         * @param id
         *         the unique ID of the tool
         */
        protected StaticAnalysisToolDescriptor(final String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        /**
         * Returns a {@link StaticAnalysisLabelProvider} that will render all tool specific labels.
         *
         * @return a tool specific {@link StaticAnalysisLabelProvider}
         */
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new StaticAnalysisLabelProvider(getId(), getDisplayName());
        }

        /**
         * Returns the default filename pattern for this tool.
         *
         * @return the default pattern
         */
        public String getPattern() {
            return StringUtils.EMPTY;
        }

        /**
         * Returns an optional help text that can provide useful hints on how to configure the static analysis tool soo that
         * the report files could be parsed by Jenkins. This help can be a plain text message or an HTML snippet.
         *
         * @return the help
         */
        public String getHelp() {
            return StringUtils.EMPTY;
        }
    }
}
