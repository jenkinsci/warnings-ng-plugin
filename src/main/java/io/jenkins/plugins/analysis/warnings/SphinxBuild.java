package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.SphinxBuildParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for Sphinx build warnings.
 *
 * @author Ullrich Hafner
 */
public class SphinxBuild extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_SphinxBuild_ParserName();

    @DataBoundConstructor
    public SphinxBuild() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new SphinxBuildParser();
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
            super("sphinx", PARSER_NAME);
        }
    }
}

