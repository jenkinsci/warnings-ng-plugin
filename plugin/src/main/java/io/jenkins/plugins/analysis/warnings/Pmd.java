package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.SvgIconLabelProvider;

/**
 * Provides a parser and customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
public class Pmd extends AnalysisModelParser {
    private static final long serialVersionUID = -7600332469176914690L;
    private static final String ID = "pmd";

    /** Creates a new instance of {@link Pmd}. */
    @DataBoundConstructor
    public Pmd() {
        super();
        // empty constructor required for stapler
    }

   /** Descriptor for this static analysis tool. */
    @Symbol("pmdParser")
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
            return new SvgIconLabelProvider(ID, getDisplayName(), getDescriptionProvider());
        }
    }
}

