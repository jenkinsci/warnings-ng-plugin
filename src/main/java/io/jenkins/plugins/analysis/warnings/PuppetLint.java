package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.PuppetLintParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Puppet Lint.
 *
 * @author Ullrich Hafner
 */
public class PuppetLint extends StaticAnalysisTool {
    private static final long serialVersionUID = 6585663572231821338L;
    static final String ID = "puppetlint";

    /** Creates a new instance of {@link PuppetLint}. */
    @DataBoundConstructor
    public PuppetLint() {
        // empty constructor required for stapler
    }

    @Override
    public PuppetLintParser createParser() {
        return new PuppetLintParser();
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
            return Messages.Warnings_Puppet_ParserName();
        }
    }
}
