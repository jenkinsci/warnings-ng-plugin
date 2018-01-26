package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.JavaDocParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.warnings.Java.JavaLabelProvider;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides customized messages for the JavaDoc parser.
 *
 * @author Ullrich Hafner
 */
@Extension
public class JavaDoc extends StaticAnalysisTool {
    static final String ID = "javadoc";
    private static final String PARSER_NAME = Messages.Warnings_JavaDoc_ParserName();

    @Override
    public IssueParser createParser() {
        return new JavaDocParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new JavaDocLabelProvider();
    }

    /** Provides the labels for the static analysis tool. */
    public static class JavaDocLabelProvider extends JavaLabelProvider {
        public JavaDocLabelProvider() {
            super(ID, PARSER_NAME);
        }
    }
}
