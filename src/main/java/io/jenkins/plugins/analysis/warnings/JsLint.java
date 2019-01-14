package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.LintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for JSLint.
 *
 * @author Ullrich Hafner
 */
public class JsLint extends ReportScanningTool {
    private static final long serialVersionUID = 1084258385562354947L;
    
    static final String ID = "jslint";

    /** Creates a new instance of {@link JsLint}. */
    @DataBoundConstructor
    public JsLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public LintParser createParser() {
        return new LintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("jsLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JSLint_ParserName();
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
