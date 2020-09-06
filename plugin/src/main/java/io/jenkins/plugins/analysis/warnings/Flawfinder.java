package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.FlawfinderParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides parsers and customized messages for Flawfinder.
 *
 * @author Dom Postorivo
 */
public class Flawfinder extends ReportScanningTool {
    private static final long serialVersionUID = 5543229182821638862L;

    private static final String ID = "flawfinder";

    /** Creates a new instance of {@link Flawfinder}. */
    @DataBoundConstructor
    public Flawfinder() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new FlawfinderParser(); 
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("flawfinder")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Flawfinder_ParserName();
        }

        @Override
        public String getHelp() {
            return "Use commandline <code>flawfinder -S</code>.";
        }

        @Override
        public String getUrl() {
            return "https://dwheeler.com/flawfinder/";
        }
    }
}
