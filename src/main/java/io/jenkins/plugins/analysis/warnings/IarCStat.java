package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.IarCStatParser;
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
public class IarCStat extends ReportScanningTool {
    private static final long serialVersionUID = 6672928932731913714L;
    static final String ID = "iarCStat";

    /** Creates a new instance of {@link IarCStat}. */
    @DataBoundConstructor
    public IarCStat() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IarCStatParser createParser() {
        return new IarCStatParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("iarCStat")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_iarCStat_ParserName();
        }

        @Override
        public String getHelp() {
            return "The IAR C-STAT static analysis tool finds potential issues in code by doing an analysis "
                    + "on the source code level.";
        }
    }
}
