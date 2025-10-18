package io.jenkins.plugins.analysis.warnings;

import hudson.Extension;
import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SymbolIconLabelProvider;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serial;

/**
 * Provides a parser and customized messages for npm audit.
 *
 * @author Ulrich Grave
 */
public class NpmAudit extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 5195421262070629304L;
    private static final String ID = "npm-audit";

    /** Creates a new instance of {@link NpmAudit}. */
    @DataBoundConstructor
    public NpmAudit() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("npmAudit")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /**
         * Creates a new instance of {@link NpmAudit.Descriptor}.
         */
        public Descriptor() {
            super(ID);
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new SymbolIconLabelProvider(ID, getDisplayName(), getDescriptionProvider(), "symbol-cib-npm plugin-warnings-ng");
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
