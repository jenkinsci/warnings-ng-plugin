package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides parsers and customized messages for DScanner.
 *
 * @author Andre Pany
 */
public class DScanner extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 7656859289383929117L;
    private static final String ID = "dscanner";

    /** Creates a new instance of {@link DScanner}. */
    @DataBoundConstructor
    public DScanner() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("dscanner")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
