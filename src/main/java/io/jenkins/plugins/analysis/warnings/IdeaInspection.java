package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.IdeaInspectionParser;
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
    }
}
