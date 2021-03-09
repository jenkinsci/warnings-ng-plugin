package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.QtTranslationParser;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser for translation files of Qt.
 *
 * @author Heiko Thiel
 *
 */
public class QtTranslation extends ReportScanningTool {
    private static final long serialVersionUID = 1L;
    private static final String ID = "qt-translation";

    /**
     * Creates a new instance of {@link QtTranslation}.
     */
    @DataBoundConstructor
    public QtTranslation() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new QtTranslationParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("qtTranslation")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getDisplayName() {
            return Messages.Warnings_QtTranslation_ParserName();
        }

        @Override
        public String getHelp() {
            return "Reads translation files of Qt, which are created by \"lupdate\" or \"Linguist\".";
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getDisplayName(), getId(), createDescriptionProvider(), "qt");
        }

        @Override
        public String getUrl() {
            return "https://www.qt.io";
        }
    }
}
