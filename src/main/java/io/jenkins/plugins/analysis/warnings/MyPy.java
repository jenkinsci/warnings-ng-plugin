package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.MyPyAdapter;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for MyPy.
 *
 * @author Ullrich Hafner
 */
public class MyPy extends ReportScanningTool {
    private static final long serialVersionUID = -1864782743893780307L;
    static final String ID = "mypy";

    /** Creates a new instance of {@link MyPy}. */
    @DataBoundConstructor
    public MyPy() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public MyPyAdapter createParser() {
        return new MyPyAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("myPy")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
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
