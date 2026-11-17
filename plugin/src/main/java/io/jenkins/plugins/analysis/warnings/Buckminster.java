package io.jenkins.plugins.analysis.warnings;

import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import java.io.Serial;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Provides a parser and customized messages for the Buckminster Compiler.
 *
 * @author Ullrich Hafner
 */
public class Buckminster extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 7067423260823622207L;

    private static final String ID = "buckminster";

    /** Creates a new instance of {@link Buckminster}. */
    @DataBoundConstructor
    public Buckminster() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("buckminster")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
