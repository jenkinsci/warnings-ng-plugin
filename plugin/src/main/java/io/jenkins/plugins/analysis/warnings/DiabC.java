package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Diab C++ compiler.
 *
 * @author Ullrich Hafner
 */
public class DiabC extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 5776036181907740586L;
    private static final String ID = "diabc";

    /** Creates a new instance of {@link DiabC}. */
    @DataBoundConstructor
    public DiabC() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("diabC")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
