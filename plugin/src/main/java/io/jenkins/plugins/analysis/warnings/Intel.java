package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Intel Compiler.
 *
 * @author Ullrich Hafner
 */
public class Intel extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 8514076930043335408L;

    private static final String ID = "intel";

    /** Creates a new instance of {@link Intel}. */
    @DataBoundConstructor
    public Intel() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("intel")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
