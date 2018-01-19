package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.XlcCompilerParser;
import edu.hm.hafner.analysis.parser.XlcLinkerParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for IBM xlC compiler and linker.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Xlc extends StaticAnalysisToolSuite {
    private static final String ID = "xlc";
    private static final String PARSER_NAME = Messages.Warnings_Xlc_ParserName();

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new XlcCompilerParser(), new XlcLinkerParser());
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
