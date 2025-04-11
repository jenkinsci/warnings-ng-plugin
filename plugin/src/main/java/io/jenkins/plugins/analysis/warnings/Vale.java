package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
* Provides parser for vale reports.
*/
public class Vale extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -251368548183118686L;
    private static final String ID = "vale";

    /** Create instance. */
    @DataBoundConstructor
    public Vale() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("vale")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Create instance. **/
        public Descriptor() {
            super(ID);
        }
    }
}
