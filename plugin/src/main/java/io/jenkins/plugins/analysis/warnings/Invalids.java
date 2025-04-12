package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Oracle Invalids.
 *
 * @author Ullrich Hafner
 */
public class Invalids extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 8400984149210830144L;
    private static final String ID = "invalids";

    /** Creates a new instance of {@link Invalids}. */
    @DataBoundConstructor
    public Invalids() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("invalids")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
