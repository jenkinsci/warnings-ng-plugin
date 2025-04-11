package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for JsHint.
 *
 * @author Ullrich Hafner
 */
public class JsHint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -871437328498798351L;
    private static final String ID = "js-hint";

    /** Creates a new instance of {@link JsHint}. */
    @DataBoundConstructor
    public JsHint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("jsHint")
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
