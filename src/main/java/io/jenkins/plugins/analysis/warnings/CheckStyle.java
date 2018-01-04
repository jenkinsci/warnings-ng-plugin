package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.steps.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.steps.StreamBasedParser;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyleRules;

import hudson.Extension;

/**
 * Provides a parser and customized messages for CheckStyle.
 *
 * @author Ullrich Hafner
 */
public class CheckStyle extends StreamBasedParser {
    private static final String PARSER_NAME = Messages.Warnings_CheckStyle_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "checkstyle-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "checkstyle-48x48.png";

    /**
     * Creates a new instance of {@link CheckStyle}.
     */
    @DataBoundConstructor
    public CheckStyle() {
        // empty constructor required for stapler
    }

    @Override
    protected AbstractParser createParser() {
        return new CheckStyleParser();
    }

    /** Registers this tool as extension point implementation. */
    @Extension
    public static final class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(new LabelProvider());
        }
    }

    /**
     * Provides the labels for the parser.
     */
    private static class LabelProvider extends DefaultLabelProvider {
        private final CheckStyleRules rules;

        LabelProvider() {
            super("checkstyle", PARSER_NAME);

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
