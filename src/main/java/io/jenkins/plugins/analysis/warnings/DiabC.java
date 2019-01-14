package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.DiabCParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Diab C++ compiler.
 *
 * @author Ullrich Hafner
 */
public class DiabC extends ReportScanningTool {
    private static final long serialVersionUID = 5776036181907740586L;
    static final String ID = "diabc";

    /** Creates a new instance of {@link DiabC}. */
    @DataBoundConstructor
    public DiabC() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public DiabCParser createParser() {
        return new DiabCParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("diabC")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_diabc_ParserName();
        }
    }
}
