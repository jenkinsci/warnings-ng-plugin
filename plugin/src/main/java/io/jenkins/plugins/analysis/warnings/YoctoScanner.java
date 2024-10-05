package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Yocto Scanner CLI (scannercli) reports.
 *
 * @author Michael Trimarchi
 */
public class YoctoScanner extends AnalysisModelParser {
    private static final long serialVersionUID = 1L;
    private static final String ID = "yoctocli";

    /**
     * Creates a new instance of {@link YoctoScanner}.
     */
    @DataBoundConstructor
    public YoctoScanner() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("yoctoScanner")
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

        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
