package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for MyPy.
 *
 * @author Ullrich Hafner
 */
public class MyPy extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -1864782743893780307L;
    private static final String ID = "mypy";

    /** Creates a new instance of {@link MyPy}. */
    @DataBoundConstructor
    public MyPy() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("myPy")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
