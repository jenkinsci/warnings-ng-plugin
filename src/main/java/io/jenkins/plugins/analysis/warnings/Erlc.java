package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.ErlcParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the ERL Compiler.
 *
 * @author Ullrich Hafner
 */
public class Erlc extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_Erlang_ParserName();

    @DataBoundConstructor
    public Erlc() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new ErlcParser();
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
            super("erlc", PARSER_NAME);
        }
    }
}
