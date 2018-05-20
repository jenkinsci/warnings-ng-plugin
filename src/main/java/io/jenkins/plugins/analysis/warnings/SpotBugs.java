package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import static hudson.plugins.warnings.WarningsDescriptor.*;

import hudson.Extension;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
public class SpotBugs extends FindBugs {
    private static final long serialVersionUID = -8773197511353021180L;
    static final String ID = "spotbugs";

    /** Creates a new instance of {@link SpotBugs}. */
    @DataBoundConstructor
    public SpotBugs() {
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    /** Provides the labels for the static analysis tool. */
    private static final class LabelProvider extends FindBugsLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";

        private LabelProvider(final FindBugsMessages messages) {
            super(messages, ID, Messages.Warnings_SpotBugs_ParserName());
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
    public static class Descriptor extends FindBugsDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_SpotBugs_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(getMessages());
        }

        @Override
        public String getPattern() {
            return "**/target/spotbugsXml.xml";
        }
    }
}
