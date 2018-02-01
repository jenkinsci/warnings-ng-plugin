package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.jcreport.JcReportParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the JcReport compiler.
 *
 * @author Johannes Arzt
 */

@Extension
public class JcReport extends StaticAnalysisTool {
    static final String ID = "jc-report";
    private static final String PARSER_NAME = Messages.Warnings_JCReport_ParserName();

    @Override
    public JcReportParser createParser() {
        return new JcReportParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
