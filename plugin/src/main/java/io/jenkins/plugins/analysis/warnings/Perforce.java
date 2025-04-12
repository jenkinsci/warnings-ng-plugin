package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Perforce tool.
 *
 * @author Joscha Behrmann
 */
public class Perforce extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 4203426682751724907L;
    private static final String ID = "perforce";

    /** Creates a new instance of {@link Perforce}. */
    @DataBoundConstructor
    public Perforce() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("perforce")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
