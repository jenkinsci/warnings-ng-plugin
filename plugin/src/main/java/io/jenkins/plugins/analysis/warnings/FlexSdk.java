package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for FLEX SDK.
 *
 * @author Ullrich Hafner
 */
public class FlexSdk extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 8786339674737448596L;
    private static final String ID = "flex";

    /** Creates a new instance of {@link FlexSdk}. */
    @DataBoundConstructor
    public FlexSdk() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("flexSdk")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
