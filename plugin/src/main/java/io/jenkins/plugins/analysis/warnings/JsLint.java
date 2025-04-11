package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for JSLint.
 *
 * @author Ullrich Hafner
 */
public class JsLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 1084258385562354947L;

    private static final String ID = "jslint";

    /** Creates a new instance of {@link JsLint}. */
    @DataBoundConstructor
    public JsLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("jsLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getUrl() {
            return "https://www.jslint.com";
        }
    }
}
