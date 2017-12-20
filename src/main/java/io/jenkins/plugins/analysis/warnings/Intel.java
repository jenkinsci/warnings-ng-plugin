package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.*;
import edu.hm.hafner.analysis.parser.IntelParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

public class Intel extends StaticAnalysisTool {

    private static final String PARSER_NAME = Messages.Warnings_IntelC_ParserName();

    @DataBoundConstructor
    public Intel() {
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder issueBuilder) throws ParsingException, ParsingCanceledException {
        return new IntelParser().parse(file, charset, issueBuilder);
    }

    /**
     * Registers this tool as extension point implementation.
     */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new Intel.LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("Intel", PARSER_NAME);
        }
    }
}

