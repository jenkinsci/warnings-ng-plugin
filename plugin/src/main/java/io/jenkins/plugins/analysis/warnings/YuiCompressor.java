package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the YUI Compressor.
 *
 * @author Ullrich Hafner
 */
public class YuiCompressor extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 4211786637477278304L;
    private static final String ID = "yui";

    /** Creates a new instance of {@link YuiCompressor}. */
    @DataBoundConstructor
    public YuiCompressor() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("yuiCompressor")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
