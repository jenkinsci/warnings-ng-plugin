package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.RuboCopParser;
import static hudson.plugins.warnings.WarningsDescriptor.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides parsers and customized messages for RuboCop.
 *
 * @author Ullrich Hafner
 */
public class RuboCop extends StaticAnalysisTool {
    static final String ID = "rubocop";

    /** Creates a new instance of {@link RuboCop}. */
    @DataBoundConstructor
    public RuboCop() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new RuboCopParser(); 
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends StaticAnalysisLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";

        LabelProvider() {
            super(ID, Messages.Warnings_RuboCop_ParserName());
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
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_RuboCop_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }
    }
}
