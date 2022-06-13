package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;


/**
 * Selects a {@link BluePearl} using the specified ID.
 *
 * @author Simon Matthews
 */
public class BluePearl extends AnalysisModelParser {
    private static final long serialVersionUID = 1L;
    private static final String ID = "bluepearl";

  /** Creates a new instance of {@link BluePearl}. */
    @DataBoundConstructor
    public BluePearl() {
        super();
        // empty constructor required for stapler
    }
    @Symbol("bluePearl")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
