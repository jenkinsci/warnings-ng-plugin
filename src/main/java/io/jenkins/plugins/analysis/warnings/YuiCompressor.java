package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.YuiCompressorParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the YUI Compressor.
 *
 * @author Ullrich Hafner
 */
public class YuiCompressor extends ReportScanningTool {
    private static final long serialVersionUID = 4211786637477278304L;
    static final String ID = "yui";

    /** Creates a new instance of {@link YuiCompressor}. */
    @DataBoundConstructor
    public YuiCompressor() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public YuiCompressorParser createParser() {
        return new YuiCompressorParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("yuiCompressor")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
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
