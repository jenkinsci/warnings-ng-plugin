package io.jenkins.plugins.analysis.warnings;

import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import java.io.Serial;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Provides a parser and customized messages for {@code dockerfile_lint} json report.
 *
 * @author Andreas Mandel
 */
public class DockerLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 42L;

    private static final String ID = "dockerlint";

    /** Creates a new instance of {@link DockerLint}. */
    @DataBoundConstructor
    public DockerLint() {
        super();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("dockerLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
