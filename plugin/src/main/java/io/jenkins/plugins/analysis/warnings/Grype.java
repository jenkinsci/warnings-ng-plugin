package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
* Provides parser for grype reports.
*/
public class Grype extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -7721519870683487886L;
    private static final String ID = "grype";

    /** Create instance. */
    @DataBoundConstructor
    public Grype() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("grype")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Create instance. **/
        public Descriptor() {
            super(ID);
        }
    }
}
