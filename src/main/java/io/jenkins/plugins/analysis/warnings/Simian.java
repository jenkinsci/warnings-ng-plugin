package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.dry.simian.SimianParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Simian duplication scanner.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Simian extends StaticAnalysisTool {
    static final String ID = "simian";
    private static final String PARSER_NAME = Messages.Warnings_Simian_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "dry-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "dry-48x48.png";
    private static final LabelProvider LABEL_PROVIDER = new LabelProvider();

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return LABEL_PROVIDER;
    }

    @Override
    public SimianParser createParser() {
        return new SimianParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        LabelProvider() {
            super(ID, PARSER_NAME);
        }

        @Override
        public String getDescription(final Issue issue) {
            return issue.getDescription();
        }

        @Override
        public String getSmallIconUrl() {
            return SMALL_ICON_URL;
        }

        @Override
        public String getLargeIconUrl() {
            return LARGE_ICON_URL;
        }
    }
}
