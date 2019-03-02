package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.IarCstatParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the IAR C-Stat static analysis tool.
 *
 * @author Lorenz Aebi
 */
public class IarCstat extends ReportScanningTool {
    private static final long serialVersionUID = 6672928932731913714L;
    static final String ID = "iar-cstat";

    /** Creates a new instance of {@link IarCstat}. */
    @DataBoundConstructor
    public IarCstat() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IarCstatParser createParser() {
        return new IarCstatParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("iarCstat")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_iarCstat_ParserName();
        }

        @Override
        public String getHelp() {
            return "The IAR C-STAT static analysis tool finds potential issues in code by doing an analysis "
                    + "on the source code level.";
        }
    }
}
