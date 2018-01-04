package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.nio.charset.Charset;

import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.parser.findbugs.FindBugsParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
public class FindBugs extends StaticAnalysisTool {
    private static final String PARSER_NAME = Messages.Warnings_CheckStyle_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "findbugs-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "findbugs-48x48.png";

    // FIXME: enum and not boolean
    private boolean useRankAsPriority;

    /**
     * Creates a new instance of {@link FindBugs}.
     */
    @DataBoundConstructor
    public FindBugs() {
        // empty constructor required for stapler
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

    public boolean getUseRankAsPriority() {
        return useRankAsPriority;
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return new FindBugsParser(useRankAsPriority).parse(file, builder);
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static final class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new FindBugsLabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static final class FindBugsLabelProvider extends DefaultLabelProvider {
        private final FindBugsMessages messages;

        private FindBugsLabelProvider() {
            super("findbugs", PARSER_NAME);

            messages = new FindBugsMessages();
            messages.initialize();
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
