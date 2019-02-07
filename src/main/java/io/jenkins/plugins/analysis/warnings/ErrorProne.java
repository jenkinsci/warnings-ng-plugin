package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.ErrorProneParser;
import edu.hm.hafner.analysis.parser.GradleErrorProneParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for Error Prone.
 *
 * @author Ullrich Hafner
 */
public class ErrorProne extends ReportScanningToolSuite {
    private static final long serialVersionUID = -511511623854186032L;
    static final String ID = "error-prone";

    /** Creates a new instance of {@link ErrorProne}. */
    @DataBoundConstructor
    public ErrorProne() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new ErrorProneParser(), new GradleErrorProneParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("errorProne")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
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
