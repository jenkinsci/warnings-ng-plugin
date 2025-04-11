package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for PyDocStyle.
 *
 * @author Ullrich Hafner
 */
public class PyDocStyle extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 6413186216055796807L;
    private static final String ID = "pydocstyle";

    /** Creates a new instance of {@link PyDocStyle}. */
    @DataBoundConstructor
    public PyDocStyle() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pyDocStyle")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
