package io.jenkins.plugins.analysis.warnings;

import org.jvnet.localizer.LocaleProvider;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.FindBugsParser;
import static edu.hm.hafner.analysis.parser.FindBugsParser.PriorityProperty.*;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
@Extension
public class FindBugs extends StaticAnalysisTool {
    private static final String ID = "findbugs";
    private static final String PARSER_NAME = Messages.Warnings_FindBugs_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";
    private static final StaticAnalysisLabelProvider LABEL_PROVIDER = new FindBugsLabelProvider();

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return LABEL_PROVIDER;
    }

    @Override
    public IssueParser createParser() {
        return new FindBugsParser(RANK);
    }

    /** Provides the labels for the static analysis tool. */
    static class FindBugsLabelProvider extends DefaultLabelProvider {
        private final FindBugsMessages messages = new FindBugsMessages();

        private FindBugsLabelProvider() {
            this(ID, PARSER_NAME);

            messages.initialize();
        }

        /**
         * Creates a new {@link FindBugsLabelProvider} with the specified ID.
         *
         * @param id
         *         the ID
         * @param name
         *         the name of the static analysis tool
         */
        protected FindBugsLabelProvider(final String id, final String name) {
            super(id, name);
        }

        @Override
        public String getDescription(final Issue issue) {
            return messages.getMessage(issue.getType(), LocaleProvider.getLocale());
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
