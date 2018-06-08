package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.TaskingVxCompilerParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for TASKING VX.
 *
 * @author Ullrich Hafner
 */
public class TaskingVx extends StaticAnalysisTool {
    private static final long serialVersionUID = -76451755325472057L;
    static final String ID = "tasking-vx";

    /** Creates a new instance of {@link TaskingVx}. */
    @DataBoundConstructor
    public TaskingVx() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public TaskingVxCompilerParser createParser() {
        return new TaskingVxCompilerParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
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
