package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.Pep8Parser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PEP8 Python style guide.
 *
 * @author Joscha Behrmann
 */
@Extension
public class Pep8 extends AbstractParserTool {
    private static final String ID = "pep8";
    private static final String PARSER_NAME = Messages.Warnings_Pep8_ParserName();

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return only(new Pep8Parser());
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
