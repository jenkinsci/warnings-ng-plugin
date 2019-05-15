package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.XmlParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser for the native format of the Warnings Next Generation Plugin.
 *
 * @author Ullrich Hafner
 */
public class WarningsPlugin extends ReportScanningTool {
    private static final long serialVersionUID = 8110398783405047555L;
    private static final String ID = "issues";

    /** Creates a new instance of {@link WarningsPlugin}. */
    @DataBoundConstructor
    public WarningsPlugin() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new XmlParser("/report/issue");
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("issues")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_WarningsPlugin_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
