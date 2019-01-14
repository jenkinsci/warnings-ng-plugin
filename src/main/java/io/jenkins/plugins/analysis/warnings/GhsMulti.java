package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.GhsMultiParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the GhsMulti Compiler.
 *
 * @author Michael Schmid
 */
public class GhsMulti extends ReportScanningTool {
    private static final long serialVersionUID = -873750719433395569L;
    static final String ID = "ghs-multi";

    /** Creates a new instance of {@link GhsMulti}. */
    @DataBoundConstructor
    public GhsMulti() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public GhsMultiParser createParser() {
        return new GhsMultiParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ghsMulti")
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

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ghs_ParserName();
        }
    }
}
