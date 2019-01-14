package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.QacSourceCodeAnalyserParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the PRQA QA-C Sourcecode Analyser.
 *
 * @author Ullrich Hafner
 */
public class QacSourceCodeAnalyser extends ReportScanningTool {
    private static final long serialVersionUID = 3092674431567484628L;
    static final String ID = "qac";

    /** Creates a new instance of {@link QacSourceCodeAnalyser}. */
    @DataBoundConstructor
    public QacSourceCodeAnalyser() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public QacSourceCodeAnalyserParser createParser() {
        return new QacSourceCodeAnalyserParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("qacSourceCodeAnalyser")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_QAC_ParserName();
        }
    }
}
