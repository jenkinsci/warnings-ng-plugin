package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.parser.NagFortranParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Provides a parser and customized messages for the NagFortran Compiler.
 *
 * @author Joscha Behrmann
 */
public class NagFortran extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_NagFortran_ParserName();

    @DataBoundConstructor
    public NagFortran() {
        // empty constructor for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder issueBuilder) {
        return new NagFortranParser().parse(file, charset, issueBuilder);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new LabelProvider());
        }
    }

    /** Provides the labels for the parser. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("NagFortran", PARSER_NAME);
        }
    }
}
