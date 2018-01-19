package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.jcreport.JcReportParser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the JcReport compiler.
 *
 * @author Johannes Arzt
 */

@Extension
public class JcReport extends AbstractParserTool {
    private static final String ID = "jc-report";
    private static final String PARSER_NAME = Messages.Warnings_JCReport_ParserName();

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return only(new JcReportParser());
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
