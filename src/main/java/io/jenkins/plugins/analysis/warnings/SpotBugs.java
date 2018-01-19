package io.jenkins.plugins.analysis.warnings;

import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
@Extension
public class SpotBugs extends FindBugs {
    private static final String ID = "spotbugs";
    private static final String PARSER_NAME = Messages.Warnings_SpotBugs_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProvider();
    }

    /** Provides the labels for the static analysis tool. */
    private static final class LabelProvider extends FindBugsLabelProvider {
        private LabelProvider() {
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
