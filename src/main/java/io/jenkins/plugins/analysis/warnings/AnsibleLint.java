package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.AnsibleLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Ansible Lint Compiler.
 *
 * @author Ullrich Hafner
 */
public class AnsibleLint extends ReportScanningTool {
    private static final long serialVersionUID = -838846658095256811L;
    static final String ID = "ansiblelint";

    /** Creates a new instance of {@link AnsibleLint}. */
    @DataBoundConstructor
    public AnsibleLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public AnsibleLintParser createParser() {
        return new AnsibleLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ansibleLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_AnsibleLint_ParserName();
        }

        @Override
        public String getHelp() {
            return "Use the flag -p.";
        }
    }
}
