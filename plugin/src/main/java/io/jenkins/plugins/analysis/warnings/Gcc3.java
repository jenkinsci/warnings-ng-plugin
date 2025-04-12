package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Gcc3 Compiler.
 *
 * @author Raphael Furch
 */
public class Gcc3 extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -8985462824184450486L;
    private static final String ID = "gcc3";

    /** Creates a new instance of {@link Gcc3}. */
    @DataBoundConstructor
    public Gcc3() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gcc3")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
