package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.SunCParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the SUN Studio C++ compiler.
 *
 * @author Ullrich Hafner
 */
public class SunC extends StaticAnalysisTool {
    private static final long serialVersionUID = -2194739612322803223L;
    static final String ID = "sunc";

    /** Creates a new instance of {@link SunC}. */
    @DataBoundConstructor
    public SunC() {
        // empty constructor required for stapler
    }

    @Override
    public SunCParser createParser() {
        return new SunCParser();
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
            return Messages.Warnings_sunc_ParserName();
        }
    }
}
