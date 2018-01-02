package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.RegexpLineParser;
import edu.hm.hafner.analysis.parser.MetrowerksCWCompilerParser;
import edu.hm.hafner.analysis.parser.MetrowerksCWLinkerParser;
import io.jenkins.plugins.analysis.core.steps.CompositeParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import static java.util.Arrays.*;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Metrowerks CodeWarrior compiler and linker.
 *
 * @author Aykut Yilmaz
 */
public class MetrowerksCodeWarrior extends CompositeParser {
    private static final String PARSER_NAME = Messages.Warnings_MetrowerksCodeWarrior_ParserName();

    @DataBoundConstructor
    public MetrowerksCodeWarrior() {
        // empty constructor required for stapler
    }

    @Override
    protected Collection<RegexpLineParser> createParsers() {
        return asList(new MetrowerksCWCompilerParser(), new MetrowerksCWLinkerParser());
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
            super("metrowerks", PARSER_NAME);
        }
    }
}

