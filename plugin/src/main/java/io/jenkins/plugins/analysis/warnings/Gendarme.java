package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Gendarme violations.
 *
 * @author Ullrich Hafner
 */
public class Gendarme extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -8528091256734714597L;
    private static final String ID = "gendarme";

    /** Creates a new instance of {@link Gendarme}. */
    @DataBoundConstructor
    public Gendarme() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gendarme")
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
