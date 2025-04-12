package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for YamlLint.
 *
 * @author Ullrich Hafner
 */
public class YamlLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 207829559393914788L;
    private static final String ID = "yamllint";

    /** Creates a new instance of {@link YamlLint}. */
    @DataBoundConstructor
    public YamlLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("yamlLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
