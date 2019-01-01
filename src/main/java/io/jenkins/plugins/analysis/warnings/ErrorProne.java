package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import edu.hm.hafner.analysis.parser.ErrorProneParser;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for Error Prone.
 *
 * @author Ullrich Hafner
 */
public class ErrorProne extends ReportScanningTool {
    private static final long serialVersionUID = -511511623854186032L;
    static final String ID = "error-prone";

    /** Creates a new instance of {@link ErrorProne}. */
    @DataBoundConstructor
    public ErrorProne() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public ErrorProneParser createParser() {
        return new ErrorProneParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("errorProne")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
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
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), "bug");
        }
        @Override
        public String getUrl() {
            return "https://errorprone.info";
        }
    }
}
