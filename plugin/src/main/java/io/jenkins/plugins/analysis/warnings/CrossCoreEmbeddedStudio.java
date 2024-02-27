package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * A parser for CrossCore Embedded Studio (CCES) from Analog Devices.
 */
public class CrossCoreEmbeddedStudio extends AnalysisModelParser {
    private static final long serialVersionUID = 1814097426285660166L;
    private static final String ID = "crosscore-embedded-studio";

    /** Creates a new instance of {@link CrossCoreEmbeddedStudio}. */
    @DataBoundConstructor
    public CrossCoreEmbeddedStudio() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("CrossCoreEmbeddedStudioParser")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
