package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
public class Pmd extends StaticAnalysisTool {
    private static final long serialVersionUID = -7600332469176914690L;
    static final String ID = "pmd";

    /** Creates a new instance of {@link Pmd}. */
    @DataBoundConstructor
    public Pmd() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public PmdParser createParser() {
        return new PmdParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends StaticAnalysisLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";

        private final PmdMessages messages;

        LabelProvider(final PmdMessages messages) {
            super(ID, Messages.Warnings_PMD_ParserName());

            this.messages = messages;
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

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        private final PmdMessages messages;

        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);

            messages = new PmdMessages();
            messages.initialize();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PMD_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(messages);
        }

        @Override
        public String getPattern() {
            return "**/pmd.xml";
        }
    }
}

