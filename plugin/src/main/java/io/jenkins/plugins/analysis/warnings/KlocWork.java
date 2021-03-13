package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.violations.KlocWorkAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Klocwork.
 *
 * @author Ullrich Hafner
 */
public class KlocWork extends AnalysisModelParser {
    private static final long serialVersionUID = -4352260844574399784L;
    private static final String ID = "klocwork";

    /** Creates a new instance of {@link KlocWork}. */
    @DataBoundConstructor
    public KlocWork() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("klocWork")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_Klocwork();
        }
    }
}
