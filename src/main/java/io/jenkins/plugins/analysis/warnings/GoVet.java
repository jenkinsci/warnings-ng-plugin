package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GoVetParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for GoVet.
 *
 * @author Ullrich Hafner
 */
public class GoVet extends ReportScanningTool {
    private static final long serialVersionUID = -4075523780782589302L;
    static final String ID = "go-vet";

    /** Creates a new instance of {@link GoVet}. */
    @DataBoundConstructor
    public GoVet() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public GoVetParser createParser() {
        return new GoVetParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("goVet")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_GoVetParser_ParserName();
        }
    }
}
