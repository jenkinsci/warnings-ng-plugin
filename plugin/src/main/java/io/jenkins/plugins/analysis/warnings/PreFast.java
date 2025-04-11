package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Microsoft PreFast.
 *
 * @author Ullrich Hafner
 */
public class PreFast extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -3802198096988685475L;
    private static final String ID = "prefast";

    /** Creates a new instance of {@link PreFast}. */
    @DataBoundConstructor
    public PreFast() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("prefast")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
