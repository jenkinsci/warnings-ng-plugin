package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.YuiCompressorParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the YUI Compressor.
 *
 * @author Ullrich Hafner
 */
public class YuiCompressor extends StaticAnalysisTool {
    private static final long serialVersionUID = 4211786637477278304L;
    static final String ID = "yui";

    /** Creates a new instance of {@link YuiCompressor}. */
    @DataBoundConstructor
    public YuiCompressor() {
        // empty constructor required for stapler
    }

    @Override
    public YuiCompressorParser createParser() {
        return new YuiCompressorParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getHelp() {
            return Messages.Warning_SlowMultiLineParser();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_YUICompressor_ParserName();
        }
    }
}
