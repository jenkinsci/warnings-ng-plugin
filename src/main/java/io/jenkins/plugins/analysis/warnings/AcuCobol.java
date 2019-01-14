package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.AcuCobolParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the AcuCobol Compiler.
 *
 * @author Ullrich Hafner
 */
public class AcuCobol extends ReportScanningTool {
    private static final long serialVersionUID = 2333849052758654239L;
    static final String ID = "acu-cobol";

    /** Creates a new instance of {@link AcuCobol}. */
    @DataBoundConstructor
    public AcuCobol() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public AcuCobolParser createParser() {
        return new AcuCobolParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("acuCobol")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_AcuCobol_ParserName();
        }
    }
}
