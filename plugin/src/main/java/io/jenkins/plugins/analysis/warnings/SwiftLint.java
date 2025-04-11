package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for SwiftLint.
 *
 * @author Ullrich Hafner
 */
public class SwiftLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -1112001682237184947L;

    private static final String ID = "swiftlint";

    /** Creates a new instance of {@link SwiftLint}. */
    @DataBoundConstructor
    public SwiftLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("swiftLint")
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
