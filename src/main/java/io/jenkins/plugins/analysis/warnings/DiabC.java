package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.DiabCParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Diab C++ compiler.
 *
 * @author Ullrich Hafner
 */
public class DiabC extends StaticAnalysisTool {
    private static final long serialVersionUID = 5776036181907740586L;
    static final String ID = "diabc";

    /** Creates a new instance of {@link DiabC}. */
    @DataBoundConstructor
    public DiabC() {
        // empty constructor required for stapler
    }

    @Override
    public DiabCParser createParser() {
        return new DiabCParser();
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
            return Messages.Warnings_diabc_ParserName();
        }
    }
}
