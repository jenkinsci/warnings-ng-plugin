package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.TrivyParser;
import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * <p>
 * aquasec Trivy is a container vulnerability scanner.
 * </p>
 * <p>
 * <strong>Usage: </strong>trivy image -f json -o results.json
 * golang:1.12-alpine
 * </p>
 *
 * @author Thomas Fürer - tfuerer.javanet@gmail.com
 *
 */
public class Trivy extends ReportScanningTool {
    private static final long serialVersionUID = 1L;

    /** construct a new instance of Trivy.**/
    @DataBoundConstructor
    public Trivy() {
        super();
    }

    @Override
    public IssueParser createParser() {
        return new TrivyParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {

        /** Creates the descriptor instance. */
        public Descriptor() {
            super("trivy-vulnerabilityscanning");
        }

        @Override
        public String getDisplayName() {
            return "aquasec trivy vulnerability scanner";
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