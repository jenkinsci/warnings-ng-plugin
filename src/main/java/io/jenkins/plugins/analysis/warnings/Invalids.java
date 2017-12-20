package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.*;
import edu.hm.hafner.analysis.parser.InvalidsParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

public class Invalids extends StaticAnalysisTool {

    private static final String PARSER_NAME = Messages.Warnings_OracleInvalids_ParserName();

    @DataBoundConstructor
    public Invalids() {
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder issueBuilder) throws ParsingException, ParsingCanceledException {
        return new InvalidsParser().parse(file, charset, issueBuilder);
    }

    /**
     * Registers this tool as extension point implementation.
     */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new Invalids.LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("Invalids", PARSER_NAME);
        }
    }
}