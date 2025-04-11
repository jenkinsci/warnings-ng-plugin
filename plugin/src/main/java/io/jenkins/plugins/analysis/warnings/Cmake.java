package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides  parser and customized messages for CMake.
 *
 * @author Ullrich Hafner
 */
public class Cmake extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -5981880343845273634L;
    private static final String ID = "cmake";

    /** Creates a new instance of {@link Cmake}. */
    @DataBoundConstructor
    public Cmake() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cmake") // FIXME: change to cmakeParse, see https://plugins.jenkins.io/cmakebuilder/
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
