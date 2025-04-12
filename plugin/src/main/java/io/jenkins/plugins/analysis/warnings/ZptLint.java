package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for ZPT-Lint.
 *
 * @author Ullrich Hafner
 */
public class ZptLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 5232724287545487246L;
    private static final String ID = "zptlint";

    /** Creates a new instance of {@link ZptLint}. */
    @DataBoundConstructor
    public ZptLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("zptLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
