package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Clang-Tidy compiler.
 *
 * @author Ullrich Hafner
 */
public class ClangTidy extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 5834065931433801829L;
    private static final String ID = "clang-tidy";

    /** Creates a new instance of {@link ClangTidy}. */
    @DataBoundConstructor
    public ClangTidy() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("clangTidy")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
