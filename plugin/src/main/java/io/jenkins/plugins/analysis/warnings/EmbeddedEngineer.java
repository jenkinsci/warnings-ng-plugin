package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * A parser for EmbeddedEngineer EA Code Generator tool.
 *
 * @author Eva Habeeb
 */
public class EmbeddedEngineer extends AnalysisModelParser {
    private static final long serialVersionUID = 1814097426285660166L;
    private static final String ID = "embedded-engineer";

    /** Creates a new instance of {@link EmbeddedEngineer}. */
    @DataBoundConstructor
    public EmbeddedEngineer() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("embeddedEngineerParser")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
