package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.RegexpLineParser;
import edu.hm.hafner.analysis.parser.XlcCompilerParser;
import edu.hm.hafner.analysis.parser.XlcLinkerParser;
import io.jenkins.plugins.analysis.core.steps.CompositeParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import static java.util.Arrays.*;

import hudson.Extension;

/**
 * Provides a parser and customized messages for IBM xlC compiler and linker.
 *
 * @author Ullrich Hafner
 */
public class Xlc extends CompositeParser {
    private static final String PARSER_NAME = Messages.Warnings_Xlc_ParserName();

    @DataBoundConstructor
    public Xlc() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<RegexpLineParser> createParsers() {
        return asList(new XlcCompilerParser(), new XlcLinkerParser());
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
                super("xlc", PARSER_NAME);
        }
    }
}
