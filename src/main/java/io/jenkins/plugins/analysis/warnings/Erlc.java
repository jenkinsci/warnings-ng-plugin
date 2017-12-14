package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.*;

import edu.hm.hafner.analysis.parser.ErlcParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

public class Erlc extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_Erlang_ParserName();

    @DataBoundConstructor
    public Erlc() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return new ErlcParser().parse(file, charset, builder);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new Erlc.LabelProvider());
        }
    }

    /** Provides the labels for the parser. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("erlc", PARSER_NAME);
        }
    }
}
