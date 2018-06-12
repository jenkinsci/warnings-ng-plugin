package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.IntelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Intel Compiler.
 *
 * @author Ullrich Hafner
 */
public class Intel extends StaticAnalysisTool {
    private static final long serialVersionUID = 8514076930043335408L;

    static final String ID = "intel";

    /** Creates a new instance of {@link Intel}. */
    @DataBoundConstructor
    public Intel() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IntelParser createParser() {
        return new IntelParser();
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
            return Messages.Warnings_Intel_ParserName();
        }
    }
}

