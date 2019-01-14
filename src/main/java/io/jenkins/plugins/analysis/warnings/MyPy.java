package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.MyPyAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

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
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_MyPy();
        }
    }
}
