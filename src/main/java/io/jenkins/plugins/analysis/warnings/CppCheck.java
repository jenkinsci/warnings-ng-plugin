package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.CppCheckAdapter;

/**
 * Provides a parser and customized messages for CPPCheck.
 *
 * @author Ullrich Hafner
 */
public class CppCheck extends StaticAnalysisTool {
    static final String ID = "cppcheck";

    /** Creates a new instance of {@link CppCheck}. */
    @DataBoundConstructor
    public CppCheck() {
        // empty constructor required for stapler
    }

    @Override
    public CppCheckAdapter createParser() {
        return new CppCheckAdapter();
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
            return Messages.Violations_CPPCheck();
        }
    }
}
