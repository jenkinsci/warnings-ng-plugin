package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.SbtScalacParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for SBT scala compiler warnings.
 *
 * @author Ullrich Hafner
 */
public class SBTScalaC extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_ScalaParser_ParserName();

    @DataBoundConstructor
    public SBTScalaC() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new SbtScalacParser();
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
            super("sbt-scala", PARSER_NAME);
        }
    }
}
