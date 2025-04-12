package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides parsers and customized messages for Flawfinder.
 *
 * @author Dom Postorivo
 */
public class Flawfinder extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 5543229182821638862L;

    private static final String ID = "flawfinder";

    /** Creates a new instance of {@link Flawfinder}. */
    @DataBoundConstructor
    public Flawfinder() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("flawfinder")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
