package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser for the Dart analyze parser.
 *
 * @author Ullrich Hafner
 */
public class Dart extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -707346657270069790L;
    private static final String ID = "dart";

    /** Creates a new instance of {@link Dart}. */
    @DataBoundConstructor
    public Dart() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("dart")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
