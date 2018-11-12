package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.jcreport.JcReportParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

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
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public JcReportParser createParser() {
        return new JcReportParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("jcReport")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JCReport_ParserName();
        }
    }
}
