package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for CodeChecker.
 */
public class CodeChecker extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -171355177448403202L;
    private static final String ID = "code-checker";

    /** Creates a new instance of {@link CodeChecker}. */
    @DataBoundConstructor
    public CodeChecker() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("codeChecker")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
