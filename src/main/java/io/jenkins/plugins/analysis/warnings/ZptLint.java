package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.ZptLintAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for ZPT-Lint.
 *
 * @author Ullrich Hafner
 */
public class ZptLint extends ReportScanningTool {
    private static final long serialVersionUID = 5232724287545487246L;
    static final String ID = "zptlint";

    /** Creates a new instance of {@link ZptLint}. */
    @DataBoundConstructor
    public ZptLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public ZptLintAdapter createParser() {
        return new ZptLintAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("zptLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_ZPTLint();
        }
    }
}
