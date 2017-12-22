package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.parser.AnsibleLintParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Provides a parser and customized messages for the Ansible Lint Compiler.
 *
 * @author Ullrich Hafner
 */
public class AnsibleLint extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_AnsibleLint_ParserName();

    @DataBoundConstructor
    public AnsibleLint() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return new AnsibleLintParser().parse(file, charset, builder);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new LabelProvider());
        }
    }

    /** Provides the labels for the parser. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("ansiblelint", PARSER_NAME);
        }
    }
}
