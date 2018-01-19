package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.Gcc4CompilerParser;
import edu.hm.hafner.analysis.parser.Gcc4LinkerParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Gcc4Compiler and Gcc4Linker parsers.
 *
 * @author Raphael Furch
 */
@Extension
public class Gcc4 extends StaticAnalysisToolSuite {
    private static final String ID = "gcc4";
    private static final String PARSER_NAME = Messages.Warnings_gcc4_ParserName();

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new Gcc4CompilerParser(), new Gcc4LinkerParser());
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
