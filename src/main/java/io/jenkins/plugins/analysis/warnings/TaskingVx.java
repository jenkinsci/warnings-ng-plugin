package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.TaskingVXCompilerParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for TASKING VX.
 *
 * @author Ullrich Hafner
 */
public class TaskingVx extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_TaskingVXCompiler_ParserName();

    @DataBoundConstructor
    public TaskingVx() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new TaskingVXCompilerParser();
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
            super("tasking-vx", PARSER_NAME);
        }
    }
}
