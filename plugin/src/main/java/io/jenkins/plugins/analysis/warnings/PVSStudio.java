package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.pvsstudio.PVSStudioParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for PVS-Studio static analyzer.
 *
 * @author PVS-Studio Team
 */
public class PVSStudio extends ReportScanningTool {
    private static final long serialVersionUID = -1114828406964963020L;

    private static final String ID = "pvs-studio"; // history chart title

    /** Creates a new instance of {@link PVSStudio}. */
    @DataBoundConstructor
    public PVSStudio() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new PVSStudioParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("PVSStudio")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "PVS-Studio";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider("PVS-Studio", ID, createDescriptionProvider(), "pvs");
        }

        @Override
        public String getPattern() {
            return "**/*.plog";
        }

        @Override
        public String getUrl() {
            return "https://www.viva64.com/en/pvs-studio/";
        }
    }
}


