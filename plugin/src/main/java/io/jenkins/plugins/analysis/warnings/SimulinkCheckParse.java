package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * A parser for Simulink Check Code Generator Report Files.
 *
 * @author Eva Habeeb
 */

public class SimulinkCheckParse extends AnalysisModelParser {

    private static final long serialVersionUID = 5776036181964740586L;
    private static final String ID = "simulink-check-parser";

    /** Creates a new instance of {@link SimulinkCheckParse}. */
    @DataBoundConstructor
    public SimulinkCheckParse() {
        super();
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
