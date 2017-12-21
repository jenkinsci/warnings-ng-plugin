package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;
import edu.hm.hafner.analysis.parser.SbtScalacParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

public class SBTScalaC extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_ScalaParser_ParserName();

    @DataBoundConstructor
    public SBTScalaC() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return new SbtScalacParser().parse(file, charset, builder);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends StaticAnalysisTool.StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new SBTScalaC.LabelProvider());
        }
    }

    /** Provides the labels for the parser. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("sbtScalac", PARSER_NAME);
        }
    }
}
