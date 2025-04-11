package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for {@code rustc} compiler messages emitted by {@code cargo check
 * --message-format json}.
 *
 * @author Ullrich Hafner
 */
public class Cargo extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -3997235880208767455L;
    private static final String ID = "cargo";

    /** Creates a new instance of {@link Cargo}. */
    @DataBoundConstructor
    public Cargo() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cargo")
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
