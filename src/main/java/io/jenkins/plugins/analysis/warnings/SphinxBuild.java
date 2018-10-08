package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.SphinxBuildParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Sphinx build warnings.
 *
 * @author Ullrich Hafner
 */
public class SphinxBuild extends StaticAnalysisTool {
    private static final long serialVersionUID = -7095926313386515100L;
    static final String ID = "sphinx";

    /** Creates a new instance of {@link SphinxBuild}. */
    @DataBoundConstructor
    public SphinxBuild() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public SphinxBuildParser createParser() {
        return new SphinxBuildParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_SphinxBuild_ParserName();
        }
    }
}

