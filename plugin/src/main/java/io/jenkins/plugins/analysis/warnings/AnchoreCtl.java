package io.jenkins.plugins.analysis.warnings;

import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import java.io.Serial;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Provides parser for anchorectl image one-time-scan vulnerability reports.
 */
public class AnchoreCtl extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 3481658723094562871L;

    private static final String ID = "anchore-ctl";

    /** Create instance. */
    @DataBoundConstructor
    public AnchoreCtl() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("anchoreCtl")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Create instance. **/
        public Descriptor() {
            super(ID);
        }
    }
}
