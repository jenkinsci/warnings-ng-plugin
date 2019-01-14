package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.TaskingVxCompilerParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for TASKING VX.
 *
 * @author Ullrich Hafner
 */
public class TaskingVx extends ReportScanningTool {
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
    @Symbol("taskingVx")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_TaskingVXCompiler_ParserName();
        }
    }
}
