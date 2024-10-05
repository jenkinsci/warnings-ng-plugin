package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides parser for OWASP dependency check reports.
 */
public class OwaspDependencyCheck extends AnalysisModelParser {
    private static final long serialVersionUID = -7721519880683487886L;

    private static final String ID = "owasp-dependency-check";

    /** Create instance. */
    @DataBoundConstructor
    public OwaspDependencyCheck() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("owaspDependencyCheck")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Create instance. **/
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
