package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.CodeNarcAdapter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for CodeNArc.
 *
 * @author Ullrich Hafner
 */
public class CodeNArc extends StaticAnalysisTool {
    private static final long serialVersionUID = 8809406805732162793L;
    static final String ID = "codenarc";

    /** Creates a new instance of {@link CodeNArc}. */
    @DataBoundConstructor
    public CodeNArc() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public CodeNarcAdapter createParser() {
        return new CodeNarcAdapter();
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
            return Messages.Violations_CodeNarc();
        }
    }
}
