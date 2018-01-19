package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.CppLintParser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for C++ Lint.
 *
 * @author Ullrich Hafner
 */
@Extension
public class CppLint extends AbstractParserTool {
    private static final String ID = "cpp-lint";
    private static final String PARSER_NAME = Messages.Warnings_CppLint_ParserName();

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return only(new CppLintParser());
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProvider();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super(ID, PARSER_NAME);
        }
    }
}
