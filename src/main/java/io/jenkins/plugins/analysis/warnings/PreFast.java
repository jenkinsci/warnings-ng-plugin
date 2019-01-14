package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.PreFastParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Microsoft PreFast.
 *
 * @author Ullrich Hafner
 */
public class PreFast extends ReportScanningTool {
    private static final long serialVersionUID = -3802198096988685475L;
    static final String ID = "prefast";

    /** Creates a new instance of {@link PreFast}. */
    @DataBoundConstructor
    public PreFast() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PreFastParser createParser() {
        return new PreFastParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("prefast")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PREfast_ParserName();
        }
    }
}
