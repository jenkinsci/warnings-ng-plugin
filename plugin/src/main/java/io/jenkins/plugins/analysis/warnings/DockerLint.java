package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.DockerLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for {@code dockerfile_lint} json report.
 *
 * @author Andreas Mandel
 */
public class DockerLint extends ReportScanningTool {
    private static final long serialVersionUID = 42L;
    private static final String ID = "dockerlint";

    /** Creates a new instance of {@link DockerLint}. */
    @DataBoundConstructor
    public DockerLint() {
        super();
    }

    @Override
    public IssueParser createParser() {
        return new DockerLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("dockerLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_DockerLint_ParserName();
        }

        @Override
        public String getHelp() {
            return "Use commandline <code>dockerfile_lint -j</code> output.<br/>"
                    + "See <a href='https://github.com/projectatomic/dockerfile_lint'>"
                    + "dockerfile_lint on Github</a> for usage details.";
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getUrl() {
            return "https://github.com/projectatomic/dockerfile_lint";
        }
    }
}
