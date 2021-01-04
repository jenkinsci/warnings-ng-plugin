package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.TrivyParser;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Aquasec Trivy is a container vulnerability scanner.
 *
 * <p>
 * Usage:
 * <pre>
 * {@code trivy image -f json -o results.json golang:1.12-alpine}
 * </pre>
 * </p>
 *
 * @author Thomas Fürer - tfuerer.javanet@gmail.com
 *
 */
public class Trivy extends ReportScanningTool {
    private static final long serialVersionUID = 1L;
    private static final String ID = "trivy";

    /**
     * Creates a new instance of {@link Trivy}.
     */
    @DataBoundConstructor
    public Trivy() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new TrivyParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("trivy")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getDisplayName() {
            return Messages.Warnings_Trivy_ParserName();
        }

        @Override
        public String getHelp() {
            return "Reads trivy json data. "
                    + "Use commandline <code>trivy image -f json -o results.json 'image'</code>"
                    + "See <a href='https://github.com/aquasecurity/trivy'>" + "tivy on Github</a> for usage details.";
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getUrl() {
            return "https://github.com/aquasecurity/trivy";
        }

        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
