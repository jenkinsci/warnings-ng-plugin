package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Clang compiler.
 *
 * @author Ullrich Hafner
 */
public class Clang extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 4179684176599641118L;
    private static final String ID = "clang";

    /** Creates a new instance of {@link Clang}. */
    @DataBoundConstructor
    public Clang() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("clang")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
