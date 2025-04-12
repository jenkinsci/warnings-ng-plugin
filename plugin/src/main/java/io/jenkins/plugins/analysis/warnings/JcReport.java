package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the JcReport compiler.
 *
 * @author Johannes Arzt
 */
public class JcReport extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -4501046255810592674L;
    private static final String ID = "jc-report";

    /** Creates a new instance of {@link JcReport}. */
    @DataBoundConstructor
    public JcReport() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("jcReport")
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
