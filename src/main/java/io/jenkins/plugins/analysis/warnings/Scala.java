package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.ScalacParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for the Scala compiler.
 *
 * @author Ullrich Hafner
 */
public class Scala extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_ScalaParser_ParserName();

    @DataBoundConstructor
    public Scala() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new ScalacParser();
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
            super("scala", PARSER_NAME);
        }
    }
}
