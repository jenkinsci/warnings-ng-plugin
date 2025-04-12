package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Php runtime errors and warnings.
 *
 * @author Ullrich Hafner
 */
public class Php extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 7286546914256953672L;
    private static final String ID = "php";

    /** Creates a new instance of {@link Php}. */
    @DataBoundConstructor
    public Php() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("php")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
