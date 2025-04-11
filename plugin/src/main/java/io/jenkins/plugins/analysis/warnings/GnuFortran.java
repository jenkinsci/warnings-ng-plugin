package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the GhsFortran Compiler.
 *
 * @author Michael Schmid
 */
public class GnuFortran extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -578099209983706725L;
    private static final String ID = "fortran";

    /** Creates a new instance of {@link GnuFortran}. */
    @DataBoundConstructor
    public GnuFortran() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gnuFortran")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
