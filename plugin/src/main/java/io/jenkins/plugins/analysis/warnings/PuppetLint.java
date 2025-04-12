package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Puppet Lint.
 *
 * @author Ullrich Hafner
 */
public class PuppetLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 6585663572231821338L;
    private static final String ID = "puppetlint";

    /** Creates a new instance of {@link PuppetLint}. */
    @DataBoundConstructor
    public PuppetLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("puppetLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
