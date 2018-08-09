package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.ErrorProneAdapter;
import static hudson.plugins.warnings.WarningsDescriptor.IMAGE_PREFIX;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Error Prone.
 *
 * @author Ullrich Hafner
 */
public class ErrorProne extends StaticAnalysisTool {
    private static final long serialVersionUID = -511511623854186032L;
    static final String ID = "error-prone";

    /** Creates a new instance of {@link ErrorProne}. */
    @DataBoundConstructor
    public ErrorProne() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public ErrorProneAdapter createParser() {
        return new ErrorProneAdapter();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends StaticAnalysisLabelProvider {
        private static final String SMALL_ICON_URL = IMAGE_PREFIX + "bug-24x24.png";
        private static final String LARGE_ICON_URL = IMAGE_PREFIX + "bug-48x48.png";

        LabelProvider() {
            super(ID, Messages.Violations_ErrorProne());
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
            return Messages.Violations_ErrorProne();
        }

        @Override
        public String getUrl() {
            return "https://errorprone.info";
        }
    }
}
