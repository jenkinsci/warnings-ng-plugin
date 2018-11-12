package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.LintParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for CSS-Lint.
 *
 * @author Ullrich Hafner
 */
public class CssLint extends ReportScanningTool {
    private static final long serialVersionUID = -2790274869830094987L;
    static final String ID = "csslint";

    /** Creates a new instance of {@link CssLint}. */
    @DataBoundConstructor
    public CssLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public LintParser createParser() {
        return new LintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cssLint")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_CssLint_ParserName();
        }
    }
}
