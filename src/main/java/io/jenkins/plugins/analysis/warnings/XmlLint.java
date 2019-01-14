package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.XmlLintAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

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

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_XmlLint();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
