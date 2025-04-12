package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Coolflux DSP Compiler.
 *
 * @author Ullrich Hafner
 */
public class Coolflux extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -6042318539034664498L;
    private static final String ID = "coolflux";

    /** Creates a new instance of {@link Coolflux}. */
    @DataBoundConstructor
    public Coolflux() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("coolflux")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
