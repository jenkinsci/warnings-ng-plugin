package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Robocopy.
 *
 * @author Ullrich Hafner
 */
public class Robocopy extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -9009703818411779941L;
    private static final String ID = "robocopy";

    /** Creates a new instance of {@link Robocopy}. */
    @DataBoundConstructor
    public Robocopy() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("robocopy")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
