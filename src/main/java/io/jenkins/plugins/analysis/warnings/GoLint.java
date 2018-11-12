package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GoLintParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for GoLint.
 *
 * @author Ullrich Hafner
 */
public class GoLint extends ReportScanningTool {
    private static final long serialVersionUID = -8739396276813816897L;
    static final String ID = "golint";

    /** Creates a new instance of {@link GoLint}. */
    @DataBoundConstructor
    public GoLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public GoLintParser createParser() {
        return new GoLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("goLint")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_GoLintParser_ParserName();
        }
       
        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
