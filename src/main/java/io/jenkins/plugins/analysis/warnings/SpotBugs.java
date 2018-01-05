package io.jenkins.plugins.analysis.warnings;

import org.kohsuke.stapler.DataBoundConstructor;

import static hudson.plugins.warnings.WarningsDescriptor.*;

import hudson.Extension;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
public class SpotBugs extends FindBugs {
    private static final String PARSER_NAME = Messages.Warnings_SpotBugs_ParserName();
    private static final String SMALL_ICON_URL = IMAGE_PREFIX + "spotbugs-24x24.png";
    private static final String LARGE_ICON_URL = IMAGE_PREFIX + "spotbugs-48x48.png";

    /**
     * Creates a new instance of {@link SpotBugs}.
     */
    @DataBoundConstructor
    public SpotBugs() {
        // empty constructor required for stapler
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
    private static final class LabelProvider extends FindBugsLabelProvider {
        private LabelProvider() {
            super("spotbugs", PARSER_NAME);
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
