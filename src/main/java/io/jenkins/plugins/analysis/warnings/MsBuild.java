package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.MsBuildParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the MsBuild Tool.
 *
 * @author Joscha Behrmann
 */
public class MsBuild extends StaticAnalysisTool {
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
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_MSBuild_ParserName();
        }
    }
}
