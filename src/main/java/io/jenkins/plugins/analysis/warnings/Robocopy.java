package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.RobocopyParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Robocopy.
 *
 * @author Ullrich Hafner
 */
public class Robocopy extends ReportScanningTool {
    private static final long serialVersionUID = -9009703818411779941L;
    static final String ID = "robocopy";

    /** Creates a new instance of {@link Robocopy}. */
    @DataBoundConstructor
    public Robocopy() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public RobocopyParser createParser() {
        return new RobocopyParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("robocopy")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Robocopy_ParserName();
        }
    }
}
