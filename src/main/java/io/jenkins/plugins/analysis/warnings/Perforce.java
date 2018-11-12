package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.P4Parser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the Perforce tool.
 *
 * @author Joscha Behrmann
 */
public class Perforce extends ReportScanningTool {
    private static final long serialVersionUID = 4203426682751724907L;
    static final String ID = "perforce";

    /** Creates a new instance of {@link Perforce}. */
    @DataBoundConstructor
    public Perforce() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public P4Parser createParser() {
        return new P4Parser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("perforce")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Perforce_ParserName();
        }
    }
}
