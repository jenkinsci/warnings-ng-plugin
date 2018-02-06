package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.MsBuildParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the MsBuild Tool.
 *
 * @author Joscha Behrmann
 */
public class MsBuild extends StaticAnalysisTool {
    static final String ID = "msbuild";

    /** Creates a new instance of {@link MsBuild}. */
    @DataBoundConstructor
    public MsBuild() {
        // empty constructor required for stapler
    }

    @Override
    public MsBuildParser createParser() {
        return new MsBuildParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
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
