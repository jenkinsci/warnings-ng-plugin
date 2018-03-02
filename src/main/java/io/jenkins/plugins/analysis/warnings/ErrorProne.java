package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.ErrorProneAdapter;

/**
 * Provides a parser and customized messages for Error Prone.
 *
 * @author Ullrich Hafner
 */
public class ErrorProne extends StaticAnalysisTool {
    static final String ID = "error-prone";

    /** Creates a new instance of {@link ErrorProne}. */
    @DataBoundConstructor
    public ErrorProne() {
        // empty constructor required for stapler
    }

    @Override
    public ErrorProneAdapter createParser() {
        return new ErrorProneAdapter();
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
            return Messages.Violations_ErrorProne();
        }
    }
}
