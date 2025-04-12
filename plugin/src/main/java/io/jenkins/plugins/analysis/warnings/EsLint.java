package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SvgIconLabelProvider;

/**
 * Provides a parser and customized messages for ESlint.
 *
 * @author Ullrich Hafner
 */
public class EsLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = -3634797822059504099L;
    private static final String ID = "eslint";

    /** Creates a new instance of {@link EsLint}. */
    @DataBoundConstructor
    public EsLint() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("esLint")
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
            return new SvgIconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
