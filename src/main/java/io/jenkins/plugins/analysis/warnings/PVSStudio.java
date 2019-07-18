package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import edu.hm.hafner.analysis.parser.pvsstudio.PVSStudioParser;

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

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends IconLabelProvider {

        LabelProvider(final String id, final String name, final String iconName) {
            super(id, name, iconName);
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("PVS-Studio")
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
        } // title page

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(ID, "PVS-Studio", "pvs");
        }

        @Override
        public String getPattern() {
            return "**/**.plog";
        }
    }

}


