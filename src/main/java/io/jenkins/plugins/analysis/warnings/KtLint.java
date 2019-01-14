package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for ktlint. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class KtLint extends ReportScanningTool {
    private static final long serialVersionUID = 1897385505660427545L;

    static final String ID = "ktlint";

    /** Creates a new instance of {@link KtLint}. */
    @DataBoundConstructor
    public KtLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ktLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_KtLint_Name();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getHelp() {
            return "Use option --reporter=checkstyle.";
        }

        @Override
        public String getUrl() {
            return "https://ktlint.github.io";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
