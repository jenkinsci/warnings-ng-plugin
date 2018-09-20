package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Infer. Delegates to {@link PmdParser}.
 *
 * @author Ullrich Hafner
 */
public class Infer extends StaticAnalysisTool {
    private static final long serialVersionUID = 1536446255698173148L;
    static final String ID = "infer";

    /** Creates a new instance of {@link Infer}. */
    @DataBoundConstructor
    public Infer() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public PmdParser createParser() {
        return new PmdParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Infer_Name();
        }

        @Override
        public String getHelp() {
            return "Use option --pmd-xml.";
        }

        @Override
        public String getUrl() {
            return "http://fbinfer.com";
        }
    }
}
