package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.DockerLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for {@code dockerfile_lint} json report.
 *
 * @author Andreas Mandel
 */
public class DockerLint extends AnalysisModelParser {
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
    public static class Descriptor extends AnalysisModelParserDescriptor {
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
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
