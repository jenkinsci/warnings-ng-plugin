package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for TSLint.
 *
 * @author Ullrich Hafner
 */
public class TsLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -2834404931238461956L;

    private static final String ID = "tslint";

    /** Creates a new instance of {@link TsLint}. */
    @DataBoundConstructor
    public TsLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("tsLint")
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
