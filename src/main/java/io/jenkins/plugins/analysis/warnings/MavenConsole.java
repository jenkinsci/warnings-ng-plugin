package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.MavenConsoleParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Maven console output.
 *
 * @author Aykut Yilmaz
 */
public class MavenConsole extends StaticAnalysisTool {
    private static final long serialVersionUID = 4642573591598798109L;
    static final String ID = "maven";

    /** Creates a new instance of {@link MavenConsole}. */
    @DataBoundConstructor
    public MavenConsole() {
        // empty constructor required for stapler
    }

    @Override
    public MavenConsoleParser createParser() {
        return new MavenConsoleParser();
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
            return Messages.Warnings_Maven_ParserName();
        }
    }
}
