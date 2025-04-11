package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the IAR C-Stat static analysis tool.
 *
 * @author Lorenz Aebi
 */
public class IarCstat extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 6672928932731913714L;
    private static final String ID = "iar-cstat";

    /** Creates a new instance of {@link IarCstat}. */
    @DataBoundConstructor
    public IarCstat() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("iarCstat")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
