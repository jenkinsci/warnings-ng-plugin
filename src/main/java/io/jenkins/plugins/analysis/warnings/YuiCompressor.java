package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.YuiCompressorParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the YUI Compressor.
 *
 * @author Ullrich Hafner
 */
@Extension
public class YuiCompressor extends StaticAnalysisTool {
    private static final String ID = "yui";
    private static final String PARSER_NAME = Messages.Warnings_YUICompressor_ParserName();

    @Override
    public IssueParser createParser() {
return new YuiCompressorParser();
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
