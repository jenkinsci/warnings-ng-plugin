package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.BrakemanParser;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Brakeman Scanner.
 */
public class Brakeman extends ReportScanningTool {
    private static final long serialVersionUID = 75319755633492904L;
    static final String ID = "brakeman";

    /** Creates a new instance of {@link Brakeman}. */
    @DataBoundConstructor
    public Brakeman() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new BrakemanParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("brakeman")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return "Brakeman";
        }

        @Override
        public String getHelp() {
            return "Reads Brakeman JSON reports. "
                    + "Use commandline <code>brakeman -o brakeman-output.json</code> output.<br/>"
                    + "See <a href='https://brakemanscanner.org/docs/jenkins/'>"
                    + "Brakeman documentation</a> for usage details.";
        }

        @Override
        public String getPattern() {
            return "brakeman-output.json";
        }

        @Override
        public String getUrl() {
            return "https://brakemanscanner.org";
        }
    }
}
