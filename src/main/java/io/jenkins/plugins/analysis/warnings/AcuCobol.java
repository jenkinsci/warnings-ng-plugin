package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.AcuCobolParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the AcuCobol Compiler.
 *
 * @author Ullrich Hafner
 */
public class AcuCobol extends StaticAnalysisTool {
    private static final long serialVersionUID = 2333849052758654239L;
    static final String ID = "acu-cobol";

    /** Creates a new instance of {@link AcuCobol}. */
    @DataBoundConstructor
    public AcuCobol() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public AcuCobolParser createParser() {
        return new AcuCobolParser();
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
            return Messages.Warnings_AcuCobol_ParserName();
        }
    }
}
