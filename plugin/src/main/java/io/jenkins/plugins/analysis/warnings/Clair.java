package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.ClairParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for {@code clair-scanner} json report.
 *
 * @author Andreas Mandel
 */
public class Clair extends ReportScanningTool {
    private static final long serialVersionUID = 42L;
    private static final String ID = "clair";

    /** Creates a new instance of {@link Clair}. */
    @DataBoundConstructor
    public Clair() {
        super();
    }

    @Override
    public IssueParser createParser() {
        return new ClairParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("clair")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Clair_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        /** No postprocessing for docker layers. */
        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
