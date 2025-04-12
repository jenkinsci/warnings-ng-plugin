package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for FxCop.
 *
 * @author Ullrich Hafner
 */
public class Fxcop extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -2406459916117372776L;
    private static final String ID = "fxcop";

    /** Creates a new instance of {@link Fxcop}. */
    @DataBoundConstructor
    public Fxcop() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("fxcop")
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
