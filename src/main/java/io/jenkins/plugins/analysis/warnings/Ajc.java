package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.AjcParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the AspectJ (ajc) Compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Ajc extends StaticAnalysisTool {
    static final String ID = "aspectj";
    private static final String PARSER_NAME = Messages.Warnings_AjcParser_ParserName();

    @Override
    public IssueParser createParser() {
        return new AjcParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProvider();
    }

    // FIXME: remove Label provider classes

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super(ID, PARSER_NAME);
        }
    }
}
