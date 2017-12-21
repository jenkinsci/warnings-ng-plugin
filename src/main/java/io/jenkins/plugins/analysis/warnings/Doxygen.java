package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.*;
import edu.hm.hafner.analysis.parser.DoxygenParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

public class Doxygen extends StaticAnalysisTool {

    private static final String PARSER_NAME = Messages.Warnings_Doxygen_ParserName();

    @DataBoundConstructor
    public Doxygen() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(File file, Charset charset, IssueBuilder issueBuilder) throws ParsingException, ParsingCanceledException {
        return new DoxygenParser().parse(file, charset, issueBuilder);
    }

    /**
     * Registers this tool as extension point implementation.
     */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new Doxygen.LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("doxygen", PARSER_NAME);
        }
    }
}
