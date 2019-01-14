package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.MsBuildParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the MsBuild Tool.
 *
 * @author Joscha Behrmann
 */
public class MsBuild extends ReportScanningTool {
    private static final long serialVersionUID = -6022797743536264094L;
    static final String ID = "msbuild";

    /** Creates a new instance of {@link MsBuild}. */
    @DataBoundConstructor
    public MsBuild() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public MsBuildParser createParser() {
        return new MsBuildParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("msBuild")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_MSBuild_ParserName();
        }
    }
}
