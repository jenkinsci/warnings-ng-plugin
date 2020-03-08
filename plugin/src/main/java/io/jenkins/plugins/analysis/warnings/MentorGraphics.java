package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.MentorParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Mentor Graphics Modelsim/Questa Simulators.
 *
 * @author Derrick Gibelyou
 */
public class MentorGraphics extends ReportScanningTool {
    private static final long serialVersionUID = 8284958840616127492L;
    private static final String ID = "modelsim";

    /** Creates a new instance of {@link MentorGraphics}. */
    @DataBoundConstructor
    public MentorGraphics() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new MentorParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("modelsim")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /**
         * Creates a new instance of {@link Descriptor}.
         */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_MentorGraphics_ParserName();
        }
    }
}
