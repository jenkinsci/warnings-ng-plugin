package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.CoolfluxChessccParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Coolflux DSP Compiler.
 *
 * @author Ullrich Hafner
 */
public class Coolflux extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_Coolflux_ParserName();

    @DataBoundConstructor
    public Coolflux() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new CoolfluxChessccParser();
    }

    /**
     * Registers this tool as extension point implementation.
     */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super("coolflux", PARSER_NAME);
        }
    }
}
