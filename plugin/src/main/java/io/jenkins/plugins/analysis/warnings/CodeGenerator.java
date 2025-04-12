package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * A parser for Simulink Code Generator tool.
 *
 * @author Eva Habeeb
 */
public class CodeGenerator extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 1814097426285660166L;
    private static final String ID = "code-generator";

    /** Creates a new instance of {@link CodeGenerator}. */
    @DataBoundConstructor
    public CodeGenerator() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("codeGeneratorParser")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
