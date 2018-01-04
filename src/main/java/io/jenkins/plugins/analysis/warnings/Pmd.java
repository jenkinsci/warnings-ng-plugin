package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
public class Pmd extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_PMD_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "pmd-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "pmd-48x48.png";

    /**
     * Creates a new instance of {@link Pmd}.
     */
    @DataBoundConstructor
    public Pmd() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new PmdParser();
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static final class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new PmdLabelProvider());
        }

    }

    /**
     * Provides the labels for the parser.
     */
    static class PmdLabelProvider extends DefaultLabelProvider {
        private final PmdMessages messages;

        PmdLabelProvider() {
            super("pmd", PARSER_NAME);

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
