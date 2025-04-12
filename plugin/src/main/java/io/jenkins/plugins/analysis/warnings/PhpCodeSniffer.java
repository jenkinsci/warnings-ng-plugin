package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for PHP_CodeSniffer.
 *
 * @author Ullrich Hafner
 */
public class PhpCodeSniffer extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -7944828406964963020L;
    private static final String ID = "php-code-sniffer";

    /** Creates a new instance of {@link PhpCodeSniffer}. */
    @DataBoundConstructor
    public PhpCodeSniffer() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("phpCodeSniffer")
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
