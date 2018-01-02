package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.ClangParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Clang compiler.
 *
 * @author Ullrich Hafner
 */
public class Clang extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_AppleLLVMClang_ParserName();

    @DataBoundConstructor
    public Clang() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new ClangParser();
    }

    /**
     * Registers this tool as extension point implementation.
     */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new Clang.LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("clang", PARSER_NAME);
        }
    }
}
