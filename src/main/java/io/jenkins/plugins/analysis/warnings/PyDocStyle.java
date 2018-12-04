package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.PyDocStyleAdapter;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for PyDocStyle.
 *
 * @author Ullrich Hafner
 */
public class PyDocStyle extends ReportScanningTool {
    private static final long serialVersionUID = 6413186216055796807L;
    static final String ID = "pydocstyle";

    /** Creates a new instance of {@link PyDocStyle}. */
    @DataBoundConstructor
    public PyDocStyle() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PyDocStyleAdapter createParser() {
        return new PyDocStyleAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pyDocStyle")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Violations_PyDocStyle();
        }
    }
}
