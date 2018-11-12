package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for ESlint. Delegates to {@link CheckStyleParser}.
 *
 * @author Ullrich Hafner
 */
public class EsLint extends ReportScanningTool {
    private static final long serialVersionUID = -3634797822059504099L;
    
    static final String ID = "eslint";

    /** Creates a new instance of {@link EsLint}. */
    @DataBoundConstructor
    public EsLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("esLint")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ESlint_Name();
        }

        @Override
        public String getHelp() {
            return "Use option --format checkstyle.";
        }

        @Override
        public String getUrl() {
            return "https://eslint.org";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
