package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.registry.ParserDescriptor.Option;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
public class FindBugs extends AnalysisModelParser {
    private static final long serialVersionUID = 4692318309214830824L;
    private static final String ID = "findbugs";

    private boolean useRankAsPriority;

    /** Creates a new instance of {@link FindBugs}. */
    @DataBoundConstructor
    public FindBugs() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Option[] configureOptions() {
        return new Option[] {
                new Option(edu.hm.hafner.analysis.registry.FindBugsDescriptor.PRIORITY_OPTION_KEY,
                        getUseRankAsPriority() ? "RANK" : "CONFIDENCE")};
    }


    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getUseRankAsPriority() {
        return useRankAsPriority;
    }

    /**
     * If useRankAsPriority is {@code true}, then the FindBugs parser will use the rank when evaluation the priority.
     * Otherwise the priority of the FindBugs warning will be mapped.
     *
     * @param useRankAsPriority
     *         {@code true} to use the rank, {@code false} to use the
     */
    @DataBoundSetter
    public void setUseRankAsPriority(final boolean useRankAsPriority) {
        this.useRankAsPriority = useRankAsPriority;
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("findBugs")
    @Extension
    public static class FindBugsDescriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public FindBugsDescriptor() {
            this(ID);
        }

        /**
         * Creates the descriptor instance.
         *
         * @param id
         *         ID of the tool
         */
        public FindBugsDescriptor(final String id) {
            super(id);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
