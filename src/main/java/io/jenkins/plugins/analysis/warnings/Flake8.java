package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.Flake8Adapter;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

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
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Violations_Flake8();
        }
    }
}
