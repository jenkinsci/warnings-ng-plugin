package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.CodeAnalysisParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the CodeAnalysis compiler.
 *
 * @author Ullrich Hafner
 */
public class CodeAnalysis extends ReportScanningTool {
    private static final long serialVersionUID = -8955858553873691807L;
    static final String ID = "code-analysis";

    /** Creates a new instance of {@link CodeAnalysis}. */
    @DataBoundConstructor
    public CodeAnalysis() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CodeAnalysisParser createParser() {
        return new CodeAnalysisParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("codeAnalysis")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_CodeAnalysis_ParserName();
        }
    }
}
