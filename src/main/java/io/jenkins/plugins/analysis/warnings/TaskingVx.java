package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.TaskingVXCompilerParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for TASKING VX.
 *
 * @author Ullrich Hafner
 */
public class TaskingVx extends StaticAnalysisTool {
    static final String ID = "tasking-vx";

    /** Creates a new instance of {@link TaskingVx}. */
    @DataBoundConstructor
    public TaskingVx() {
        // empty constructor required for stapler
    }

    @Override
    public TaskingVXCompilerParser createParser() {
        return new TaskingVXCompilerParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_TaskingVXCompiler_ParserName();
        }
    }
}
