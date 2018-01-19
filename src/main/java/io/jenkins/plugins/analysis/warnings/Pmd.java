package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Pmd extends AbstractParserTool {
    private static final String ID = "pmd";
    private static final String PARSER_NAME = Messages.Warnings_PMD_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";
    private static final LabelProvider LABEL_PROVIDER = new LabelProvider();

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return LABEL_PROVIDER;
    }

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return only(new PmdParser());
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        private final PmdMessages messages;

        LabelProvider() {
            super(ID, PARSER_NAME);

            messages = new PmdMessages();
            messages.initialize();
        }

        @Override
        public String getDescription(final Issue issue) {
            return messages.getMessage(issue.getCategory(), issue.getType());
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
