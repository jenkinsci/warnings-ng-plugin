package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for IBM xlC compiler and linker.
 *
 * @author Ullrich Hafner
 */
public class Xlc extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -3811101878455857601L;
    private static final String ID = "xlc";

    /** Creates a new instance of {@link Xlc}. */
    @DataBoundConstructor
    public Xlc() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("xlc")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
