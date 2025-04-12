package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Aquasec Trivy is a container vulnerability scanner.
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * {@code trivy image -f json -o results.json golang:1.12-alpine}
 * </pre>
 *
 * @author Thomas Fürer - tfuerer.javanet@gmail.com
 *
 */
public class Trivy extends AnalysisModelParser {
    @Serial
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

    /** Descriptor for this static analysis tool. */
    @Symbol("trivy")
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

        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
