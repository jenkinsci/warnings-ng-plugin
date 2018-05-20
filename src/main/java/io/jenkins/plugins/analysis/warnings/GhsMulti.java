package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GhsMultiParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the GhsMulti Compiler.
 *
 * @author Michael Schmid
 */
public class GhsMulti extends StaticAnalysisTool {
    private static final long serialVersionUID = -873750719433395569L;
    static final String ID = "ghs-multi";

    /** Creates a new instance of {@link GhsMulti}. */
    @DataBoundConstructor
    public GhsMulti() {
        // empty constructor required for stapler
    }

    @Override
    public GhsMultiParser createParser() {
        return new GhsMultiParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getHelp() {
            return Messages.Warning_SlowMultiLineParser();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ghs_ParserName();
        }
    }
}
