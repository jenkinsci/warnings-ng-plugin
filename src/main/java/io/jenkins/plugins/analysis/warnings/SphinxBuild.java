package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.SphinxBuildParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for Sphinx build warnings.
 *
 * @author Ullrich Hafner
 */
@Extension
public class SphinxBuild extends StaticAnalysisTool {
    private static final String ID = "sphinx";
    private static final String PARSER_NAME = Messages.Warnings_SphinxBuild_ParserName();

    @Override
    public IssueParser createParser() {
return new SphinxBuildParser();
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

