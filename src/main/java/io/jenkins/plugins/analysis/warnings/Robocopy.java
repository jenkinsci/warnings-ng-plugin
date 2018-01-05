package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.RobocopyParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StreamBasedParser;

import hudson.Extension;
import hudson.plugins.warnings.parser.Messages;

/**
 * Provides a parser and customized messages for Robocopy.
 *
 * @author Ullrich Hafner
 */
public class Robocopy extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_Robocopy_ParserName();

    @DataBoundConstructor
    public Robocopy() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new RobocopyParser();
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
            super("robocopy", PARSER_NAME);
        }
    }
}