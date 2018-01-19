package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyleRules;

import hudson.Extension;

/**
 * Provides a parser and customized messages for CheckStyle.
 *
 * @author Ullrich Hafner
 */
@Extension
public class CheckStyle extends StaticAnalysisTool {
    private static final String ID = "checkstyle";
    private static final String PARSER_NAME = Messages.Warnings_CheckStyle_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";
    private static final LabelProvider LABEL_PROVIDER = new LabelProvider();

    @Override
    public IssueParser createParser() {
return new CheckStyleParser();
}

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return LABEL_PROVIDER;
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        private final CheckStyleRules rules;

        LabelProvider() {
            super(ID, PARSER_NAME);

            rules = new CheckStyleRules();
            rules.initialize();
        }

        @Override
        public String getDescription(final Issue issue) {
            return rules.getDescription(issue.getType());
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
