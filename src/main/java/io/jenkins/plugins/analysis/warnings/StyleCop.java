package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.StyleCopParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for StyleCop.
 *
 * @author Ullrich Hafner
 */
public class StyleCop extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_StyleCop_ParserName();

    @DataBoundConstructor
    public StyleCop() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new StyleCopParser();
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
            super("stylecop", PARSER_NAME);
        }
    }
}
