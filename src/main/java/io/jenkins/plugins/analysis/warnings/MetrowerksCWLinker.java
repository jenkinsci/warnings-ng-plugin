package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.MetrowerksCWLinkerParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the MetrowerksCWLinker.
 *
 * @author Aykut Yilmaz
 */
public class MetrowerksCWLinker extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_MetrowerksCWLinker_ParserName();

    @DataBoundConstructor
    public MetrowerksCWLinker() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new MetrowerksCWLinkerParser();
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
            super("metrowerks-l", PARSER_NAME);
        }
    }
}
