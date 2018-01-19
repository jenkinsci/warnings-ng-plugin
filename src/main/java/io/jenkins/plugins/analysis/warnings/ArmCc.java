package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.Armcc5CompilerParser;
import edu.hm.hafner.analysis.parser.ArmccCompilerParser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Buckminster Compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class ArmCc extends AbstractParserTool {
    private static final String ID = "armcc";
    private static final String PARSER_NAME = Messages.Warnings_Armcc_ParserName();

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return all(new Armcc5CompilerParser(), new ArmccCompilerParser());
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
