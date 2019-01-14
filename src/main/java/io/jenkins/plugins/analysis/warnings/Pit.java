package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.PitAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for PIT.
 *
 * @author Ullrich Hafner
 */
public class Pit extends ReportScanningTool {
    private static final long serialVersionUID = -3769283356498049888L;
    static final String ID = "pit";

    /** Creates a new instance of {@link Pit}. */
    @DataBoundConstructor
    public Pit() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PitAdapter createParser() {
        return new PitAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pit")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_PIT();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }

        @Override
        public String getUrl() {
            return "http://pitest.org";
        }
    }
}
