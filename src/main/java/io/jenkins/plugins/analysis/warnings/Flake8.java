package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.Flake8Adapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Android Lint.
 *
 * @author Ullrich Hafner
 */
public class Flake8 extends ReportScanningTool {
    private static final long serialVersionUID = 2133173655608279071L;
    static final String ID = "flake8";

    /** Creates a new instance of {@link Flake8}. */
    @DataBoundConstructor
    public Flake8() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public Flake8Adapter createParser() {
        return new Flake8Adapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("flake8")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_Flake8();
        }

        @Override
        public String getHelp() {
            return "<p>Run flake8 as <code>flake8 --format=pylint</code></p>";
        }
    }
}
