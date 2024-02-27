package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * A parser for Simulink Check code generator report files.
 *
 * @author Eva Habeeb
 */
public class SimulinkCheck extends AnalysisModelParser {
    private static final long serialVersionUID = 1814097426285660166L;
    private static final String ID = "simulink-check-parser";

    /** Creates a new instance of {@link SimulinkCheck}. */
    @DataBoundConstructor
    public SimulinkCheck() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("simulinkCheckParser")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
