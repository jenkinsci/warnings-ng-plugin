package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.SbtScalacParser;
import edu.hm.hafner.analysis.parser.ScalacParser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for the Scala compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Scala extends AbstractParserTool {
    private static final String ID = "scala";
    private static final String PARSER_NAME = Messages.Warnings_ScalaParser_ParserName();

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return all(new ScalacParser(), new SbtScalacParser());
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
