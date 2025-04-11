package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser for SARIF.
 *
 * @author Ullrich Hafner
 */
public class Sarif extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 4682615727923018497L;
    private static final String ID = "sarif";

    /** Creates a new instance of {@link Sarif}. */
    @DataBoundConstructor
    public Sarif() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("sarif")
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
