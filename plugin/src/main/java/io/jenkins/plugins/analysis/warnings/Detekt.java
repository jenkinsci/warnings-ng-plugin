package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Detekt.
 *
 * @author Ullrich Hafner
 */
public class Detekt extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 2441989609462884392L;

    private static final String ID = "detekt";

    /** Creates a new instance of {@link Detekt}. */
    @DataBoundConstructor
    public Detekt() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("detekt")
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
