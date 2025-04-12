package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser for the Blue Pearl Software Visual Verification tool.
 *
 * @author Simon Matthews
 */
public class BluePearl extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String ID = "bluepearl";

    /** Creates a new instance of {@link BluePearl}. */
    @DataBoundConstructor
    public BluePearl() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("bluepearl")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /**
         * Creates a new instance of {@link Descriptor}.
         */
        public Descriptor() {
            super(ID);
        }
    }
}
