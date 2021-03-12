package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.ClangAnalyzerPlistParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the clang-analyzer.
 *
 * @author Andrey Danin
 */
public class ClangAnalyzer extends ReportScanningTool {
    private static final long serialVersionUID = 1L;
    private static final String ID = "clang-analyzer";

    /** Creates a new instance of {@link ClangAnalyzer}. */
    @DataBoundConstructor
    public ClangAnalyzer() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new ClangAnalyzerPlistParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("clangAnalyzer")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ClangAnalyzer_ParserName();
        }
    }
}
