package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Infer. Delegates to {@link PmdParser}.
 *
 * @author Ullrich Hafner
 */
public class Infer extends ReportScanningTool {
    private static final long serialVersionUID = 1536446255698173148L;
    static final String ID = "infer";

    /** Creates a new instance of {@link Infer}. */
    @DataBoundConstructor
    public Infer() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PmdParser createParser() {
        return new PmdParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("infer")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
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
