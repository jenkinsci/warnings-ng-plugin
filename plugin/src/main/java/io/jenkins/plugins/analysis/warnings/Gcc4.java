package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Gcc4Compiler and Gcc4Linker parsers.
 *
 * @author Raphael Furch
 */
public class Gcc4 extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 7699675509414211993L;
    private static final String ID = "gcc";

    /** Creates a new instance of {@link Gcc4}. */
    @DataBoundConstructor
    public Gcc4() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol({"gcc", "gcc4"})
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
