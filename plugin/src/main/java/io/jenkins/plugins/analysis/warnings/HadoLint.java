package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.HadoLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for {@code hadolint} json report.
 *
 * @author Andreas Mandel
 */
public class HadoLint extends ReportScanningTool {
    private static final long serialVersionUID = 42L;
    private static final String ID = "hadolint";

    /** Creates a new instance of {@link HadoLint}. */
    @DataBoundConstructor
    public HadoLint() {
        super();
    }

    @Override
    public IssueParser createParser() {
        return new HadoLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("hadoLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_HadoLint_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), createDescriptionProvider());
        }
    }
}
