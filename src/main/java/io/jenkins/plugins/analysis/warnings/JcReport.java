package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.jcreport.JcReportParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the JcReport compiler.
 *
 * @author Johannes Arzt
 */
public class JcReport extends ReportScanningTool {
    private static final long serialVersionUID = -4501046255810592674L;
    static final String ID = "jc-report";

    /** Creates a new instance of {@link JcReport}. */
    @DataBoundConstructor
    public JcReport() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public JcReportParser createParser() {
        return new JcReportParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("jcReport")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JCReport_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
