package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for CodeNarc.
 *
 * @author Ullrich Hafner
 */
public class CodeNarc extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 8809406805732162793L;
    private static final String ID = "codenarc";

    /** Creates a new instance of {@link CodeNarc}. */
    @DataBoundConstructor
    public CodeNarc() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("codeNarc")
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
