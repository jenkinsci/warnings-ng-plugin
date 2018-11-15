package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.CoolfluxChessccParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the Coolflux DSP Compiler.
 *
 * @author Ullrich Hafner
 */
public class Coolflux extends ReportScanningTool {
    private static final long serialVersionUID = -6042318539034664498L;
    static final String ID = "coolflux";

    /** Creates a new instance of {@link Coolflux}. */
    @DataBoundConstructor
    public Coolflux() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CoolfluxChessccParser createParser() {
        return new CoolfluxChessccParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("coolflux")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Coolflux_ParserName();
        }
    }
}
