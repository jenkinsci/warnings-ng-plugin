package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the PC-Lint Tool.
 *
 * @author Ullrich Hafner
 */
public class PcLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -6022797743536264094L;
    private static final String ID = "pclint";

    /** Creates a new instance of {@link PcLint}. */
    @DataBoundConstructor
    public PcLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pcLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
