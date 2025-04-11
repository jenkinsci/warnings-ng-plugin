package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the IAR C/C++ compiler.
 *
 * @author Ullrich Hafner
 */
public class Iar extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 6672928932731913714L;
    private static final String ID = "iar";

    /** Creates a new instance of {@link Iar}. */
    @DataBoundConstructor
    public Iar() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("iar")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
