package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the GhsMulti Compiler.
 *
 * @author Michael Schmid
 */
public class GhsMulti extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -873750719433395569L;
    private static final String ID = "ghs-multi";

    /** Creates a new instance of {@link GhsMulti}. */
    @DataBoundConstructor
    public GhsMulti() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ghsMulti")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
