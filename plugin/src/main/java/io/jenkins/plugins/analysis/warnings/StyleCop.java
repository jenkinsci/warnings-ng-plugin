package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;

import hudson.Extension;

import java.io.Serial;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for StyleCop.
 *
 * @author Ullrich Hafner
 */
public class StyleCop extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 230834175319040103L;
    private static final String ID = "stylecop";

    /** Creates a new instance of {@link StyleCop}. */
    @DataBoundConstructor
    public StyleCop() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("styleCop")
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
