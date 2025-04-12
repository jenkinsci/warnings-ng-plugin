package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for CPPCheck.
 *
 * @author Ullrich Hafner
 */
public class CppCheck extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -5646367160520640291L;
    private static final String ID = "cppcheck";

    /** Creates a new instance of {@link CppCheck}. */
    @DataBoundConstructor
    public CppCheck() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cppCheck")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
