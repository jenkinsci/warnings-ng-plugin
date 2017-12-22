package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.MavenConsoleParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Maven console output.
 *
 * @author Aykut Yilmaz
 */
public class MavenConsole extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_Maven_ParserName();

    @DataBoundConstructor
    public MavenConsole() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new MavenConsoleParser();
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
            super("maven", PARSER_NAME);
        }
    }
}
