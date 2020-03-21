package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.DScannerParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides parsers and customized messages for DScanner.
 *
 * @author Andre Pany
 */
public class DScanner extends ReportScanningTool {
    private static final long serialVersionUID = 7656859289383929117L;
    private static final String ID = "dscanner";

    /** Creates a new instance of {@link DScanner}. */
    @DataBoundConstructor
    public DScanner() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new DScannerParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("dscanner")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_DScanner_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getPattern() {
            return "**/dscanner-report.json";
        }

        @Override
        public String getUrl() {
            return "https://github.com/dlang-community/D-Scanner";
        }
    }
}
