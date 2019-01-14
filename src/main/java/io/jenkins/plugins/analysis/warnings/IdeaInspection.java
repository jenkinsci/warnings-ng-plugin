package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.IdeaInspectionParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for IDEA Inspections.
 *
 * @author Ullrich Hafner
 */
public class IdeaInspection extends ReportScanningTool {
    private static final long serialVersionUID = 6473299663127011037L;
    static final String ID = "idea";

    /** Creates a new instance of {@link IdeaInspection}. */
    @DataBoundConstructor
    public IdeaInspection() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IdeaInspectionParser createParser() {
        return new IdeaInspectionParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ideaInspection")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_IdeaInspection_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getUrl() {
            return "https://www.jetbrains.com/help/idea/code-inspection.html";
        }
    }
}
