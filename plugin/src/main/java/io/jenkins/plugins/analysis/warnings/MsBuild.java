package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the MsBuild Tool.
 *
 * @author Joscha Behrmann
 */
public class MsBuild extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -6022797743536264094L;
    private static final String ID = "msbuild";

    /** Creates a new instance of {@link MsBuild}. */
    @DataBoundConstructor
    public MsBuild() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("msBuild")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
