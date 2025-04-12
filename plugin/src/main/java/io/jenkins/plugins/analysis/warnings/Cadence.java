package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Cadence Incisive Enterprise Simulator.
 *
 * @author Ullrich Hafner
 */
public class Cadence extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 8284958840616127492L;
    private static final String ID = "cadence";

    /** Creates a new instance of {@link Cadence}. */
    @DataBoundConstructor
    public Cadence() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cadence")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /**
         * Creates a new instance of {@link Descriptor}.
         */
        public Descriptor() {
            super(ID);
        }
    }
}
