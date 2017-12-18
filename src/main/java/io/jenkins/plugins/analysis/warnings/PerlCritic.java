package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.*;
import edu.hm.hafner.analysis.parser.PerlCriticParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

public class PerlCritic extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_PerlCritic_ParserName();

    @DataBoundConstructor
    public PerlCritic() {
        // empty constructor required for stapler
    }

    /**
     * Registers this tool as extension point implementation.
     */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("perlcritic", PARSER_NAME);
        }
    }

    @Override
    public Issues<Issue> parse(File file, Charset charset, IssueBuilder builder) throws ParsingException, ParsingCanceledException {
        return new PerlCriticParser().parse(file, charset, builder);
    }
}
