package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Cpplint.
 *
 * @author Ullrich Hafner
 */
public class CppLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 7156745296954706641L;
    private static final String ID = "cpplint";

    /** Creates a new instance of {@link CppLint}. */
    @DataBoundConstructor
    public CppLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cppLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
