package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
public class Pmd extends ReportScanningTool {
    private static final long serialVersionUID = -7600332469176914690L;
    private static final String ID = "pmd";

    /** Creates a new instance of {@link Pmd}. */
    @DataBoundConstructor
    public Pmd() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new PmdParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pmdParser")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PMD_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(ID, Messages.Warnings_PMD_ParserName(), createDescriptionProvider());
        }
    }
}

