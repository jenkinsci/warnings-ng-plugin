package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.TaskingVXCompilerParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for TASKING VX.
 *
 * @author Ullrich Hafner
 */
@Extension
public class TaskingVx extends StaticAnalysisTool {
    static final String ID = "tasking-vx";
    private static final String PARSER_NAME = Messages.Warnings_TaskingVXCompiler_ParserName();

    @Override
    public IssueParser createParser() {
        return new TaskingVXCompilerParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
