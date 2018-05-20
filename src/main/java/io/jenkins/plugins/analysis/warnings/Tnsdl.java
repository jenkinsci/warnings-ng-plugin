package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.TnsdlParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Texas Instruments Code Composer Studio compiler.
 *
 * @author Ullrich Hafner
 */
public class Tnsdl extends StaticAnalysisTool {
    private static final long serialVersionUID = 3738252418578966192L;
    static final String ID = "tnsdl";

    /** Creates a new instance of {@link Tnsdl}. */
    @DataBoundConstructor
    public Tnsdl() {
        // empty constructor required for stapler
    }

    @Override
    public TnsdlParser createParser() {
        return new TnsdlParser();
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
            return Messages.Warnings_TNSDL_ParserName();
        }
    }
}
