package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GccParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Gcc3 Compiler.
 *
 * @author Raphael Furch
 */
public class Gcc3 extends StaticAnalysisTool {
    private static final long serialVersionUID = -8985462824184450486L;
    static final String ID = "gcc3";

    /** Creates a new instance of {@link Gcc3}. */
    @DataBoundConstructor
    public Gcc3() {
        // empty constructor required for stapler
    }

    @Override
    public GccParser createParser() {
        return new GccParser();
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
            return Messages.Warnings_gcc3_ParserName();
        }
    }
}
