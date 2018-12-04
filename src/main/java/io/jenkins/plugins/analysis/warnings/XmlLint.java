package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.XmlLintAdapter;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for XML-Lint.
 *
 * @author Ullrich Hafner
 */
public class XmlLint extends ReportScanningTool {
    private static final long serialVersionUID = -8253765174954652451L;
    static final String ID = "xmllint";

    /** Creates a new instance of {@link XmlLint}. */
    @DataBoundConstructor
    public XmlLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public XmlLintAdapter createParser() {
        return new XmlLintAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("xmlLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Violations_XmlLint();
        }
    }
}
