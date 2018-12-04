package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.RobocopyParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Robocopy_ParserName();
        }
    }
}
