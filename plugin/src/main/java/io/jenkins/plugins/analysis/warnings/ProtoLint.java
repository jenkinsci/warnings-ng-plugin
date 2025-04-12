package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for ProtoLint.
 *
 * @author David Hart
 */
public class ProtoLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -5718503998068521571L;

    private static final String ID = "protolint";

    /** Creates a new instance of {@link ProtoLint}. */
    @DataBoundConstructor
    public ProtoLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("protoLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getUrl() {
            return "https://github.com/yoheimuta/protolint";
        }
    }
}
