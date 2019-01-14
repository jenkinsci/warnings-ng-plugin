package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.SunCParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the SUN Studio C++ compiler.
 *
 * @author Ullrich Hafner
 */
public class SunC extends ReportScanningTool {
    private static final long serialVersionUID = -2194739612322803223L;
    static final String ID = "sunc";

    /** Creates a new instance of {@link SunC}. */
    @DataBoundConstructor
    public SunC() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public SunCParser createParser() {
        return new SunCParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("sunC")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_sunc_ParserName();
        }
    }
}
