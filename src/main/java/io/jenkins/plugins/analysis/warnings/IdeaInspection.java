package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.IdeaInspectionParser;
import static hudson.plugins.warnings.WarningsDescriptor.IMAGE_PREFIX;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for IDEA Inspections.
 *
 * @author Ullrich Hafner
 */
public class IdeaInspection extends StaticAnalysisTool {
    private static final long serialVersionUID = 6473299663127011037L;
    static final String ID = "idea";

    /** Creates a new instance of {@link IdeaInspection}. */
    @DataBoundConstructor
    public IdeaInspection() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public IdeaInspectionParser createParser() {
        return new IdeaInspectionParser();
    }
   
    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends StaticAnalysisLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";

        LabelProvider() {
            super(ID, hudson.plugins.warnings.parser.Messages.Warnings_IdeaInspection_ParserName());
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
            return Messages.Warnings_IdeaInspection_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }

        @Override
        public String getUrl() {
            return "https://www.jetbrains.com/help/idea/code-inspection.html";
        }
    }
}
