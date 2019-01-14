package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.TiCcsParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Texas Instruments Code Composer Studio compiler.
 *
 * @author Ullrich Hafner
 */
public class TiCss extends ReportScanningTool {
    private static final long serialVersionUID = -7982604310730169143L;
    static final String ID = "code-composer";

    /** Creates a new instance of {@link TiCss}. */
    @DataBoundConstructor
    public TiCss() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public TiCcsParser createParser() {
        return new TiCcsParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("tiCss")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_TexasI_ParserName();
        }
    }
}
