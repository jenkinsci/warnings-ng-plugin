package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for PHPStan. Delegates to {@link CheckStyleParser}.
 *
 * @author Jeroen Jans
 */
public class PhpStan extends ReportScanningTool {
    private static final long serialVersionUID = 2699509705079011738L;

    private static final String ID = "phpstan";

    /** Creates a new instance of {@link PhpStan}. */
    @DataBoundConstructor
    public PhpStan() {
        super();
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("phpStan")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PhpStan_Name();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getHelp() {
            return "Use the options: --no-progress --error-format checkstyle";
        }

        @Override
        public String getUrl() {
            return "https://github.com/phpstan/phpstan";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
