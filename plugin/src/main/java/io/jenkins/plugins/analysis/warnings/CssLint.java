package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for CSS-Lint.
 *
 * @author Ullrich Hafner
 */
public class CssLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -2790274869830094987L;
    private static final String ID = "csslint";

    /** Creates a new instance of {@link CssLint}. */
    @DataBoundConstructor
    public CssLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cssLint")
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
