package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.JsHintAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for JsHint.
 *
 * @author Ullrich Hafner
 */
public class JsHint extends ReportScanningTool {
    private static final long serialVersionUID = -871437328498798351L;
    static final String ID = "js-hint";

    /** Creates a new instance of {@link JsHint}. */
    @DataBoundConstructor
    public JsHint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public JsHintAdapter createParser() {
        return new JsHintAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("jsHint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_JSHint();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
