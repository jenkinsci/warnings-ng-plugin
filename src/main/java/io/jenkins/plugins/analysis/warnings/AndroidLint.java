package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.AndroidLintParserAdapter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Android Lint.
 *
 * @author Ullrich Hafner
 */
public class AndroidLint extends StaticAnalysisTool {
    private static final long serialVersionUID = -7264992947534927156L;
    static final String ID = "android-lint";

    /** Creates a new instance of {@link AndroidLint}. */
    @DataBoundConstructor
    public AndroidLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public AndroidLintParserAdapter createParser() {
        return new AndroidLintParserAdapter();
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
            return Messages.Violations_AndroidLint();
        }
    }
}
