package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.PuppetLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Puppet Lint.
 *
 * @author Ullrich Hafner
 */
public class PuppetLint extends ReportScanningTool {
    private static final long serialVersionUID = 6585663572231821338L;
    static final String ID = "puppetlint";

    /** Creates a new instance of {@link PuppetLint}. */
    @DataBoundConstructor
    public PuppetLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PuppetLintParser createParser() {
        return new PuppetLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("puppetLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Puppet_ParserName();
        }

        @Override
        public String getHelp() {
            return "  <p>\n"
                    + "    To be able to grab puppet-lint output, you will need a recent enough version that supports\n"
                    + "    the <code>--log-format flag</code>.<br/>\n"
                    + "    When running puppet-lint, make sure you use the following log format:\n"
                    + "  </p>\n"
                    + "  <pre>\n"
                    + "    <code>%{path}:%{line}:%{check}:%{KIND}:%{message}</code>\n"
                    + "  </pre>\n"
                    + "  <p>Complete example:</p>\n"
                    + "  <pre>\n"
                    + "      <code>find . -iname *.pp -exec puppet-lint --log-format \"%{path}:%{line}:%{check}:%{KIND}:%{message}\" {} \\;</code>\n"
                    + "  </pre>\n";
        }
    }
}
