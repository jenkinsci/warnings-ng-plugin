package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the SUN Studio C++ compiler.
 *
 * @author Ullrich Hafner
 */
public class SunC extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -2194739612322803223L;
    private static final String ID = "sunc";

    /** Creates a new instance of {@link SunC}. */
    @DataBoundConstructor
    public SunC() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("sunC")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
