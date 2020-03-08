package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.violations.CppCheckAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for CPPCheck.
 *
 * @author Ullrich Hafner
 */
public class CppCheck extends ReportScanningTool {
    private static final long serialVersionUID = -5646367160520640291L;
    private static final String ID = "cppcheck";

    /** Creates a new instance of {@link CppCheck}. */
    @DataBoundConstructor
    public CppCheck() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new CppCheckAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("cppCheck")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_CPPCheck();
        }

        @Override
        public String getHelp() {
            return "Use options --xml --xml-version=2";
        }
    }
}
