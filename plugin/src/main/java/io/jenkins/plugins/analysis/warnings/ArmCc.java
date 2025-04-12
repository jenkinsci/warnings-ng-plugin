package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the ArmCc compiler.
 *
 * @author Ullrich Hafner
 */
public class ArmCc extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 5712079077224290879L;
    private static final String ID = "armcc";

    /** Creates a new instance of {@link ArmCc}. */
    @DataBoundConstructor
    public ArmCc() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("armCc")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
