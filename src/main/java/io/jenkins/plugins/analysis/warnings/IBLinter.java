package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for IBLinter. Delegates to {@link CheckStyleParser}.
 *
 * @author Paweł Madej
 */
public class IBLinter extends ReportScanningTool {
    private static final long serialVersionUID = -1112001682237184947L;

    private static final String ID = "iblinter";

    /** Creates a new instance of {@link IBLinter}. */
    @DataBoundConstructor
    public IBLinter() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("IBLinter")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_IBLinter_Name();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getHelp() {
            return "Use configuration reporter: \\”checkstyle\\”.";
        }

        @Override
        public String getUrl() {
            return "https://github.com/IBDecodable/IBLinter";
        }
    }
}
