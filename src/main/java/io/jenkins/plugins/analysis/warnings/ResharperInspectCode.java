package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.ResharperInspectCodeParser;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Resharper Inspections.
 *
 * @author Ullrich Hafner
 */
public class ResharperInspectCode extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_ReshaperInspectCode_ParserName();

    @DataBoundConstructor
    public ResharperInspectCode() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new ResharperInspectCodeParser();
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
            super("resharperInspectCode", PARSER_NAME);
        }
    }

}
