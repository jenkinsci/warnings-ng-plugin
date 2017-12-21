package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.*;
import edu.hm.hafner.analysis.parser.LinuxKernelOutputParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Provides a parser and customized messages for the LinuxKernelOutput compiler.
 *
 * @author Johannes Arzt
 */

public class LinuxKernelOutput extends StaticAnalysisTool {

    private static final String PARSER_NAME = Messages.Warnings_LinuxKernelOutput_ParserName();

    @DataBoundConstructor
    public LinuxKernelOutput() {
    }

    @Override
    public Issues<Issue> parse(File file, Charset charset, IssueBuilder issueBuilder) throws ParsingException, ParsingCanceledException {
        return new LinuxKernelOutputParser().parse(file,charset,issueBuilder);
    }



    /**
     * Registers this tool as extension point implementation.
     */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new LinuxKernelOutput.LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("LinuxKernelOutput", PARSER_NAME);
        }
    }
}
