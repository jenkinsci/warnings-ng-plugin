package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.LinuxKernelOutputParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the LinuxKernelOutput compiler.
 *
 * @author Johannes Arzt
 */
@Extension
public class LinuxKernelOutput extends StaticAnalysisTool {
    static final String ID = "linux";
    private static final String PARSER_NAME = Messages.Warnings_LinuxKernelOutput_ParserName();

    @Override
    public IssueParser createParser() {
        return new LinuxKernelOutputParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
