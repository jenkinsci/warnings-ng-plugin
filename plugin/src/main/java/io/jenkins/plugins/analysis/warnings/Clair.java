package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for {@code clair-scanner} json report.
 *
 * @author Andreas Mandel
 */
public class Clair extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 42L;
    private static final String ID = "clair";

    /** Creates a new instance of {@link Clair}. */
    @DataBoundConstructor
    public Clair() {
        super();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("clair")
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

        /** No postprocessing for docker layers. */
        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
