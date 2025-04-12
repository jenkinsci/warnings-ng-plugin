package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Aqua Scanner CLI (scannercli) reports.
 *
 * @author Juri Duval
 */
public class AquaScanner extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String ID = "scannercli";

    /**
     * Creates a new instance of {@link AquaScanner}.
     */
    @DataBoundConstructor
    public AquaScanner() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("aquaScanner")
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
