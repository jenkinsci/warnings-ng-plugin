package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Infer. Delegates to {@link PmdParser}.
 *
 * @author Ullrich Hafner
 */
public class Infer extends AnalysisModelParser {
    private static final long serialVersionUID = 1536446255698173148L;
    private static final String ID = "infer";

    /** Creates a new instance of {@link Infer}. */
    @DataBoundConstructor
    public Infer() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("infer")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Infer_Name();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
