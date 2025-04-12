package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for CCM.
 *
 * @author Ullrich Hafner
 */
public class Ccm extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 7286546914256953672L;
    private static final String ID = "ccm";

    /** Creates a new instance of {@link Ccm}. */
    @DataBoundConstructor
    public Ccm() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ccm")
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
