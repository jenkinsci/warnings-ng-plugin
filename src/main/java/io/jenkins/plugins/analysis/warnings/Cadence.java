package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.CadenceIncisiveParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Cadence Incisive Enterprise Simulator.
 *
 * @author Ullrich Hafner
 */
public class Cadence extends ReportScanningTool {
    private static final long serialVersionUID = 8284958840616127492L;
    static final String ID = "cadence";

    /** Creates a new instance of {@link Cadence}. */
    @DataBoundConstructor
    public Cadence() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CadenceIncisiveParser createParser() {
        return new CadenceIncisiveParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cadence")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /**
         * Creates a new instance of {@link Descriptor}.
         */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_CadenceIncisive_ParserName();
        }
    }
}
