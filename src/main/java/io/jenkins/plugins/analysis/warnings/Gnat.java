package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GnatParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Gnat Compiler.
 *
 * @author Michael Schmid
 */
public class Gnat extends StaticAnalysisTool {
    static final String ID = "gnat";

    /** Creates a new instance of {@link Gnat}. */
    @DataBoundConstructor
    public Gnat() {
        // empty constructor required for stapler
    }

    @Override
    public GnatParser createParser() {
        return new GnatParser();
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
            return Messages.Warnings_gnat_ParserName();
        }
    }
}
