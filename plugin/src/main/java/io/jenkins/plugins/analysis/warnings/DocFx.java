package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for DocFX.
 *
 * @author Ullrich Hafner
 */
public class DocFx extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 268538556620830869L;
    private static final String ID = "docfx";

    /** Creates a new instance of {@link DocFx}. */
    @DataBoundConstructor
    public DocFx() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("docFx")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
