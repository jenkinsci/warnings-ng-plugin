package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.YamlLintAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for YamlLint.
 *
 * @author Ullrich Hafner
 */
public class YamlLint extends ReportScanningTool {
    private static final long serialVersionUID = 207829559393914788L;
    static final String ID = "yamllint";

    /** Creates a new instance of {@link YamlLint}. */
    @DataBoundConstructor
    public YamlLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public YamlLintAdapter createParser() {
        return new YamlLintAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("yamlLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_YamlLint_Name();
        }

        @Override
        public String getUrl() {
            return "https://yamllint.readthedocs.io/";
        }
    }
}
