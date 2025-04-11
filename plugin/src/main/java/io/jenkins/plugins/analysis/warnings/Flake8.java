package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Flake8.
 *
 * @author Ullrich Hafner
 */
public class Flake8 extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 2133173655608279071L;
    private static final String ID = "flake8";

    /** Creates a new instance of {@link Flake8}. */
    @DataBoundConstructor
    public Flake8() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("flake8")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
