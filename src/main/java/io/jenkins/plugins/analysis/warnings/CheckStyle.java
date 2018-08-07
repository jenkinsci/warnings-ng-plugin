package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyleRules;

import hudson.Extension;

/**
 * Provides a parser and customized messages for CheckStyle.
 *
 * @author Ullrich Hafner
 */
public class CheckStyle extends StaticAnalysisTool {
    private static final long serialVersionUID = -7944828406964963020L;
    static final String ID = "checkstyle";

    /** Creates a new instance of {@link CheckStyle}. */
    @DataBoundConstructor
    public CheckStyle() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends StaticAnalysisLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";

        private final CheckStyleRules rules;

        LabelProvider(final CheckStyleRules rules) {
            super(ID, Messages.Warnings_CheckStyle_ParserName());

            this.rules = rules;
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

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        private final CheckStyleRules rules;

        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);

            rules = new CheckStyleRules();
            rules.initialize();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_CheckStyle_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(rules);
        }

        @Override
        public String getPattern() {
            return "**/checkstyle-result.xml";
        }

        @Override
        public String getUrl() {
            return "https://checkstyle.org";
        }
    }
}
