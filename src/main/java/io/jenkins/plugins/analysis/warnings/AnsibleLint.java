package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.AnsibleLintParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Ansible Lint Compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class AnsibleLint extends StaticAnalysisTool {
    static final String ID = "ansible-lint";
    private static final String PARSER_NAME = Messages.Warnings_AnsibleLint_ParserName();

    @Override
    public AnsibleLintParser createParser() {
        return new AnsibleLintParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
