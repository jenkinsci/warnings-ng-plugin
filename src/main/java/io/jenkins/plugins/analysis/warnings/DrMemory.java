package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.nio.charset.Charset;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.parser.DrMemoryParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Dr. Memory Errors.
 *
 * @author Ullrich Hafner
 */
public class DrMemory extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_DrMemory_ParserName();

    @DataBoundConstructor
    public DrMemory() {
        // empty constructor required for stapler
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder)
            throws ParsingException, ParsingCanceledException {
        return new DrMemoryParser().parse(file, charset, builder);
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
            super("drmemory", PARSER_NAME);
        }
    }
}
