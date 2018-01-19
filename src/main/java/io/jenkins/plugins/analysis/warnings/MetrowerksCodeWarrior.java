package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.MetrowerksCWCompilerParser;
import edu.hm.hafner.analysis.parser.MetrowerksCWLinkerParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Metrowerks CodeWarrior compiler and linker.
 *
 * @author Aykut Yilmaz
 */
@Extension
public class MetrowerksCodeWarrior extends StaticAnalysisToolSuite {
    private static final String ID = "metrowerks";
    private static final String PARSER_NAME = Messages.Warnings_MetrowerksCodeWarrior_ParserName();

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new MetrowerksCWCompilerParser(), new MetrowerksCWLinkerParser());
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

