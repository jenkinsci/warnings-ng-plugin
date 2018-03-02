package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.MyPyAdapter;

/**
 * Provides a parser and customized messages for MyPy.
 *
 * @author Ullrich Hafner
 */
public class MyPy extends StaticAnalysisTool {
    static final String ID = "mypy";

    /** Creates a new instance of {@link MyPy}. */
    @DataBoundConstructor
    public MyPy() {
        // empty constructor required for stapler
    }

    @Override
    public MyPyAdapter createParser() {
        return new MyPyAdapter();
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
            return Messages.Violations_MyPy();
        }
    }
}
