package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.Pep8Parser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PEP8 Python style guide.
 *
 * @author Joscha Behrmann
 */
public class Pep8 extends StaticAnalysisTool {
    private static final long serialVersionUID = -2199589729419226931L;
    static final String ID = "pep8";

    /** Creates a new instance of {@link NagFortran}. */
    @DataBoundConstructor
    public Pep8() {
        // empty constructor required for stapler
    }

    @Override
    public Pep8Parser createParser() {
        return new Pep8Parser();
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
            return Messages.Warnings_Pep8_ParserName();
        }
    }
}
