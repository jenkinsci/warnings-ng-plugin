package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.PyLintParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PyLint.
 *
 * @author Ullrich Hafner
 */
@Extension
public class PyLint extends StaticAnalysisTool {
    static final String ID = "pylint";
    private static final String PARSER_NAME = Messages.Warnings_PyLint_ParserName();

    @Override
    public IssueParser createParser() {
        return new PyLintParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
