package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GnuMakeGccParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the GnuMakeGcc Compiler.
 *
 * @author Michael Schmid
 */
public class GnuMakeGcc extends StaticAnalysisTool {
    private static final long serialVersionUID = -5332481308142256483L;
    static final String ID = "gmake-gcc";

    /** Creates a new instance of {@link GnuMakeGcc}. */
    @DataBoundConstructor
    public GnuMakeGcc() {
        // empty constructor required for stapler
    }

    @Override
    public GnuMakeGccParser createParser() {
        return new GnuMakeGccParser();
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
            return Messages.Warnings_GnuMakeGcc_ParserName();
        }
    }
}
