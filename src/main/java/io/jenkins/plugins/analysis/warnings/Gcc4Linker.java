package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.Gcc4LinkerParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Gcc4Linker Compiler.
 *
 * @author Raphael Furch
 */
public class Gcc4Linker extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_gcc4_ParserName();

    @DataBoundConstructor
    public Gcc4Linker() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new Gcc4LinkerParser();
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
            super("gcc4-linker", PARSER_NAME);
        }
    }
}
