package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;

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

    /** Descriptor for {@link StaticAnalysisTool}. **/
    public abstract static class StaticAnalysisToolDescriptor extends Descriptor<StaticAnalysisTool> {
        private final String id;

        public StaticAnalysisToolDescriptor(final String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        public StaticAnalysisLabelProvider getLabelProvider() {
            return new DefaultLabelProvider(getId(), getDisplayName());
        }
    }
}
