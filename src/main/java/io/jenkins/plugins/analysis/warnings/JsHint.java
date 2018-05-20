package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.JsHintAdapter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for JsHint.
 *
 * @author Ullrich Hafner
 */
public class JsHint extends StaticAnalysisTool {
    private static final long serialVersionUID = -871437328498798351L;
    static final String ID = "js-hint";

    /** Creates a new instance of {@link JsHint}. */
    @DataBoundConstructor
    public JsHint() {
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public JsHintAdapter createParser() {
        return new JsHintAdapter();
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
            return Messages.Violations_JSHint();
        }
    }
}
