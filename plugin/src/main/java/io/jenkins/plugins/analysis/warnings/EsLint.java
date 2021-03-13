package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for ESlint. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class EsLint extends AnalysisModelParser {
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

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ESlint_Name();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
