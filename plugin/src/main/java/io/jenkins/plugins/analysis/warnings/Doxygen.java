package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Doxygen.
 *
 * @author Ullrich Hafner
 */
public class Doxygen extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -958188599615335136L;
    private static final String ID = "doxygen";

    /** Creates a new instance of {@link Doxygen}. */
    @DataBoundConstructor
    public Doxygen() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("doxygen")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
