package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Eclipse Compiler.
 *
 * @author Ullrich Hafner
 */
public class Eclipse extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -2312612497121380654L;
    private static final String ID = "eclipse";

    /** Creates a new instance of {@link Eclipse}. */
    @DataBoundConstructor
    public Eclipse() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("eclipse")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
