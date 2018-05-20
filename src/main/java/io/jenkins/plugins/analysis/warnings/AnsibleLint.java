package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.AnsibleLintParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Ansible Lint Compiler.
 *
 * @author Ullrich Hafner
 */
public class AnsibleLint extends StaticAnalysisTool {
    private static final long serialVersionUID = -838846658095256811L;
    static final String ID = "ansible-lint";

    /** Creates a new instance of {@link AnsibleLint}. */
    @DataBoundConstructor
    public AnsibleLint() {
        // empty constructor required for stapler
    }

    @Override
    public AnsibleLintParser createParser() {
        return new AnsibleLintParser();
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
            return Messages.Warnings_AnsibleLint_ParserName();
        }
    }
}
