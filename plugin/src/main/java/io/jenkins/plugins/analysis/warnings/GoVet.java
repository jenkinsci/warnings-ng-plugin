package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for GoVet.
 *
 * @author Ullrich Hafner
 */
public class GoVet extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -4075523780782589302L;
    private static final String ID = "go-vet";

    /** Creates a new instance of {@link GoVet}. */
    @DataBoundConstructor
    public GoVet() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("goVet")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
