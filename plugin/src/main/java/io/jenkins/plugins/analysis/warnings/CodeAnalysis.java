package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the CodeAnalysis compiler.
 *
 * @author Ullrich Hafner
 */
public class CodeAnalysis extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -8955858553873691807L;
    private static final String ID = "code-analysis";

    /** Creates a new instance of {@link CodeAnalysis}. */
    @DataBoundConstructor
    public CodeAnalysis() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("codeAnalysis")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
