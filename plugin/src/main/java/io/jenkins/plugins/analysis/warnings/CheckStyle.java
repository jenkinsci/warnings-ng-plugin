package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;

/**
 * Provides a parser and customized messages for CheckStyle.
 *
 * @author Ullrich Hafner
 */
public class CheckStyle extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -7944828406964963020L;
    private static final String ID = "checkstyle";

    /** Creates a new instance of {@link CheckStyle}. */
    @DataBoundConstructor
    public CheckStyle() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("checkStyle")
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

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new SymbolIconLabelProvider(ID, getDisplayName(), getDescriptionProvider(), "symbol-checkstyle plugin-warnings-ng");
        }
    }
}
