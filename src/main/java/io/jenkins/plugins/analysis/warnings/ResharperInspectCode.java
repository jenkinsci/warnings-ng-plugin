package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.ResharperInspectCodeAdapter;
import static hudson.plugins.warnings.WarningsDescriptor.IMAGE_PREFIX;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Resharper Inspections.
 *
 * @author Ullrich Hafner
 */
public class ResharperInspectCode extends StaticAnalysisTool {
    private static final long serialVersionUID = 7249388335877895890L;
    static final String ID = "resharper";

    /** Creates a new instance of {@link ResharperInspectCode}. */
    @DataBoundConstructor
    public ResharperInspectCode() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public ResharperInspectCodeAdapter createParser() {
        return new ResharperInspectCodeAdapter();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends StaticAnalysisLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + ID + "-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + ID + "-48x48.png";

        LabelProvider() {
            super(ID, hudson.plugins.warnings.parser.Messages.Warnings_ScalaParser_ParserName());
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
            return Messages.Warnings_ReshaperInspectCode_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider();
        }
    }
}
