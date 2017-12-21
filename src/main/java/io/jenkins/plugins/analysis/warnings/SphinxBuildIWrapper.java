package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;
import edu.hm.hafner.analysis.parser.SphinxBuildParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

public class SphinxBuildIWrapper extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_SphinxBuild_ParserName();

    @DataBoundConstructor
    public SphinxBuildIWrapper() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return new SphinxBuildParser().parse(file, charset, builder);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new SphinxBuildIWrapper.LabelProvider());
        }
    }

    /** Provides the labels for the parser. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("sphinxBuild", PARSER_NAME);
        }
    }
}

