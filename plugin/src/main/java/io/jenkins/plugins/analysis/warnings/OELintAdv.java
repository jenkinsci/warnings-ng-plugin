package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides parsers and customized messages for oelint-adv.
 */
public class OELintAdv extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String ID = "oelint-adv";

    /** Creates a new instance of {@link OELintAdv}. */
    @DataBoundConstructor
    public OELintAdv() {
        super();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("oelintAdv")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
