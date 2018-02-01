package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.dry.cpd.CpdParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for CPD.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Cpd extends StaticAnalysisTool {
    static final String ID = "cpd";
    private static final String PARSER_NAME = Messages.Warnings_CPD_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "dry-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "dry-48x48.png";

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProvider();
    }

    @Override
    public CpdParser createParser() {
        return new CpdParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        LabelProvider() {
            super(ID, PARSER_NAME);
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
