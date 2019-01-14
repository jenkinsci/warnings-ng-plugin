package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.gendarme.GendarmeParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Gendarme violations.
 *
 * @author Ullrich Hafner
 */
public class Gendarme extends ReportScanningTool {
    private static final long serialVersionUID = -8528091256734714597L;
    static final String ID = "gendarme";

    /** Creates a new instance of {@link Gendarme}. */
    @DataBoundConstructor
    public Gendarme() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public GendarmeParser createParser() {
        return new GendarmeParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gendarme")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Gendarme_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
