package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Veracode Pipeline Scanner reports.
 *
 * @author Juri Duval
 */
public class VeracodePipelineScanner extends AnalysisModelParser {
    private static final long serialVersionUID = 1L;
    private static final String ID = "veracode-pipeline-scanner";

    /**
     * Creates a new instance of {@link VeracodePipelineScanner}.
     */
    @DataBoundConstructor
    public VeracodePipelineScanner() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("veracodePipelineScanner")
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
