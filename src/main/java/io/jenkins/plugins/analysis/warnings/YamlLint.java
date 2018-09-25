package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.YamlLintAdapter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for YamlLint.
 *
 * @author Ullrich Hafner
 */
public class YamlLint extends StaticAnalysisTool {
    private static final long serialVersionUID = 207829559393914788L;
    static final String ID = "yamllint";

    /** Creates a new instance of {@link YamlLint}. */
    @DataBoundConstructor
    public YamlLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public YamlLintAdapter createParser() {
        return new YamlLintAdapter();
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
            return Messages.Warnings_YamlLint_Name();
        }

        @Override
        public String getUrl() {
            return "https://yamllint.readthedocs.io/";
        }
    }
}
