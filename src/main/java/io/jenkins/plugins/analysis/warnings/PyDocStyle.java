package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.PyDocStyleAdapter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PyDocStyle.
 *
 * @author Ullrich Hafner
 */
public class PyDocStyle extends StaticAnalysisTool {
    private static final long serialVersionUID = 6413186216055796807L;
    static final String ID = "pydocstyle";

    /** Creates a new instance of {@link PyDocStyle}. */
    @DataBoundConstructor
    public PyDocStyle() {
        // empty constructor required for stapler
    }

    @Override
    public PyDocStyleAdapter createParser() {
        return new PyDocStyleAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
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
