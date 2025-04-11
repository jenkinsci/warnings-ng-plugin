package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the ERL Compiler.
 *
 * @author Ullrich Hafner
 */
public class Erlc extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 8254330761908676605L;
    private static final String ID = "erlc";

    /** Creates a new instance of {@link Erlc}. */
    @DataBoundConstructor
    public Erlc() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("erlc")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
