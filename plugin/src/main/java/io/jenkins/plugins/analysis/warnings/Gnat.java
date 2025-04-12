package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Gnat Compiler.
 *
 * @author Michael Schmid
 */
public class Gnat extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 1249773597483641464L;
    private static final String ID = "gnat";

    /** Creates a new instance of {@link Gnat}. */
    @DataBoundConstructor
    public Gnat() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gnat")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
