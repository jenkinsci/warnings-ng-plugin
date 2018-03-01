package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import static edu.hm.hafner.analysis.parser.FindBugsParser.PriorityProperty.*;
import static hudson.plugins.warnings.WarningsDescriptor.*;

import hudson.Extension;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.FindBugsParser;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
public class FindBugs extends StaticAnalysisTool {
    static final String ID = "findbugs";

    private boolean useRankAsPriority;

    /** Creates a new instance of {@link FindBugs}. */
    @DataBoundConstructor
    public FindBugs() {
        // empty constructor required for stapler
    }

    public boolean getUseRankAsPriority() {
        return useRankAsPriority;
    }

    /**
     * If useRankAsPriority is {@code true}, then the FindBugs parser will use the rank when evaluation the priority.
     * Otherwise the priority of the FindBugs warning will be mapped.
     *
     * @param useRankAsPriority
     *         {@code true} to use the rank, {@code false} to use the
     */
    @DataBoundSetter
    public void setUseRankAsPriority(final boolean useRankAsPriority) {
        this.useRankAsPriority = useRankAsPriority;
    }

    @Override
    public FindBugsParser createParser() {
        return new FindBugsParser(RANK);
    }

    /** Provides the labels for the static analysis tool. */
    static class FindBugsLabelProvider extends StaticAnalysisLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";
        private final FindBugsMessages messages;

        private FindBugsLabelProvider(final FindBugsMessages messages) {
            this(messages, ID, Messages.Warnings_FindBugs_ParserName());
        }

        /**
         * Creates a new {@link FindBugsLabelProvider} with the specified ID.
         *
         * @param id
         *         the ID
         * @param name
         *         the name of the static analysis tool
         */
        protected FindBugsLabelProvider(final FindBugsMessages messages, final String id, final String name) {
            super(id, name);

            this.messages = messages;
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

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class FindBugsDescriptor extends StaticAnalysisToolDescriptor {
        private final FindBugsMessages messages = new FindBugsMessages();

        public FindBugsDescriptor() {
            this(ID);
        }

        public FindBugsDescriptor(final String id) {
            super(id);

            messages.initialize();
        }

        protected FindBugsMessages getMessages() {
            return messages;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_FindBugs_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new FindBugsLabelProvider(messages);
        }
    }
}
