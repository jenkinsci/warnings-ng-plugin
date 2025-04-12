package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for XML-Lint.
 *
 * @author Ullrich Hafner
 */
public class XmlLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -8253765174954652451L;
    private static final String ID = "xmllint";

    /** Creates a new instance of {@link XmlLint}. */
    @DataBoundConstructor
    public XmlLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("xmlLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
