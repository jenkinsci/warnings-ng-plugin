package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.RegexpLineParser;
import edu.hm.hafner.analysis.parser.Gcc4CompilerParser;
import edu.hm.hafner.analysis.parser.Gcc4LinkerParser;
import io.jenkins.plugins.analysis.core.steps.CompositeParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import static java.util.Arrays.*;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Gcc4Compiler and Gcc4Linker parsers.
 *
 * @author Raphael Furch
 */
public class Gcc4 extends CompositeParser {
    private static final String PARSER_NAME = Messages.Warnings_gcc4_ParserName();

    @DataBoundConstructor
    public Gcc4() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<RegexpLineParser> createParsers() {
        return asList(new Gcc4CompilerParser(), new Gcc4LinkerParser());
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
            super("gcc4-compiler", PARSER_NAME);
        }
    }
}
