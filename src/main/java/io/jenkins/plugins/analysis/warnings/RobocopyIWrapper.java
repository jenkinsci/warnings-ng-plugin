package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.parser.EclipseParser;
import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;
import hudson.plugins.warnings.parser.RobocopyParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class RobocopyIWrapper extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_Robocopy_ParserName();

    @DataBoundConstructor
    public RobocopyIWrapper() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return new RobocopyParser().parse(new FileReader(file)); // TODO
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends StaticAnalysisTool.StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new RobocopyIWrapper.LabelProvider());
        }
    }

    /** Provides the labels for the parser. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("robocopy", PARSER_NAME);
        }
    }
}