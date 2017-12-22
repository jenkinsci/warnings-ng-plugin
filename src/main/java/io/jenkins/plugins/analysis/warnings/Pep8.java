package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.Pep8Parser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PEP8 Python style guide.
 *
 * @author Joscha Behrmann
 */
public class Pep8 extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_Pep8_ParserName();

    @DataBoundConstructor
    public Pep8() {
        // empty constructor for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new Pep8Parser();
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
            super("pep8", PARSER_NAME);
        }
    }
}
