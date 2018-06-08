package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.TiCcsParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Texas Instruments Code Composer Studio compiler.
 *
 * @author Ullrich Hafner
 */
public class TiCss extends StaticAnalysisTool {
    private static final long serialVersionUID = -7982604310730169143L;
    static final String ID = "code-composer";

    /** Creates a new instance of {@link TiCss}. */
    @DataBoundConstructor
    public TiCss() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public TiCcsParser createParser() {
        return new TiCcsParser();
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
            return Messages.Warnings_TexasI_ParserName();
        }
    }
}
